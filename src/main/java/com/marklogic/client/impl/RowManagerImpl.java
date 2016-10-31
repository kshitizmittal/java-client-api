/*
 * Copyright 2016 MarkLogic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.marklogic.client.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marklogic.client.DatabaseClientFactory.HandleFactoryRegistry;
import com.marklogic.client.MarkLogicBindingException;
import com.marklogic.client.MarkLogicIOException;
import com.marklogic.client.MarkLogicInternalException;
import com.marklogic.client.Transaction;
import com.marklogic.client.expression.PlanBuilder;
import com.marklogic.client.expression.PlanBuilder.Plan;
import com.marklogic.client.impl.RESTServices.RESTServiceResult;
import com.marklogic.client.impl.RESTServices.RESTServiceResultIterator;
import com.marklogic.client.io.BaseHandle;
import com.marklogic.client.io.Format;
import com.marklogic.client.io.InputStreamHandle;
import com.marklogic.client.io.XMLStreamReaderHandle;
import com.marklogic.client.io.marker.AbstractReadHandle;
import com.marklogic.client.io.marker.AbstractWriteHandle;
import com.marklogic.client.io.marker.ContentHandle;
import com.marklogic.client.io.marker.JSONWriteHandle;
import com.marklogic.client.io.marker.StructureReadHandle;
import com.marklogic.client.row.RawPlanDefinition;
import com.marklogic.client.row.RowManager;
import com.marklogic.client.row.RowRecord;
import com.marklogic.client.row.RowSet;
import com.marklogic.client.type.PlanParam;
import com.marklogic.client.type.XsAnyAtomicTypeVal;
import com.marklogic.client.util.RequestParameters;

public class RowManagerImpl
    extends AbstractLoggingManager
    implements RowManager
{
    private RESTServices services;
	private HandleFactoryRegistry handleRegistry;

	public RowManagerImpl(RESTServices services) {
		super();
		this.services = services;
	}

	HandleFactoryRegistry getHandleRegistry() {
		return handleRegistry;
	}
	void setHandleRegistry(HandleFactoryRegistry handleRegistry) {
		this.handleRegistry = handleRegistry;
	}

	@Override
	public PlanBuilder newPlanBuilder() {
		XsExprImpl xs = new XsExprImpl();

		PlanBuilderImpl planBuilder = new PlanBuilderImpl(
				new CtsExprImpl(xs),  new FnExprImpl(xs),    new JsonExprImpl(xs),
				new MapExprImpl(xs),  new MathExprImpl(xs),  new RdfExprImpl(xs),
				new SemExprImpl(xs),  new SpellExprImpl(xs), new SqlExprImpl(xs),
				new XdmpExprImpl(xs), xs
				);

		planBuilder.setHandleRegistry(getHandleRegistry());

		return planBuilder;
	}

	@Override
	public RawPlanDefinition newRawPlanDefinition(JSONWriteHandle handle) {
		return new RawPlanDefinitionImpl(handle);
	}

	@Override
	public <T> T resultDocAs(Plan plan, Class<T> as) {
		return resultDocAs(plan, as, null, ColumnTypes.ROWS);
	}
	@Override
	public <T> T resultDocAs(Plan plan, Class<T> as, Transaction transaction) {
		return resultDocAs(plan, as, transaction, ColumnTypes.ROWS);
	}
	@Override
	public <T> T resultDocAs(Plan plan, Class<T> as, Transaction transaction, ColumnTypes typeLocation) {
		ContentHandle<T> handle = handleFor(as); 
	    if (resultDoc(plan, (StructureReadHandle) handle, transaction, typeLocation) == null) {
	    	return null;
	    }

		return handle.get();
	}
	@Override
	public <T extends StructureReadHandle> T resultDoc(Plan plan, T resultsHandle) {
		return resultDoc(plan, resultsHandle, null, ColumnTypes.ROWS);
	}
	@Override
	public <T extends StructureReadHandle> T resultDoc(Plan plan, T resultsHandle, Transaction transaction) {
		return resultDoc(plan, resultsHandle, transaction, ColumnTypes.ROWS);
	}
	@Override
	public <T extends StructureReadHandle> T resultDoc(Plan plan, T resultsHandle, Transaction transaction, ColumnTypes typeLocation) {
		PlanBuilderBase.RequestPlan requestPlan = checkPlan(plan);

		AbstractWriteHandle astHandle = requestPlan.getHandle();

		if (resultsHandle == null) {
			throw new IllegalArgumentException("Must specify a handle to read the row result document");
		}

		RequestParameters params = getParamBindings(requestPlan);
		if (ColumnTypes.HEADER == typeLocation) {
			params.add("column-types", "header");
		}

		return services.postResource(requestLogger, "rows", transaction, params, astHandle, resultsHandle);
	}

	@Override
	public RowSet<RowRecord> resultRows(Plan plan) {
		return resultRows(plan, (Transaction) null);
	}
	@Override
	public RowSet<RowRecord> resultRows(Plan plan, Transaction transaction) {
		RESTServiceResultIterator iter = makeRequest(plan, "json", "reference", transaction, ColumnTypes.ROWS);

		return new RowSetRecord("json", iter, getHandleRegistry());
	}
	@Override
	public <T extends StructureReadHandle> RowSet<T> resultRows(Plan plan, T rowHandle) {
		return resultRows(plan, rowHandle, (Transaction) null, ColumnTypes.ROWS);
	}
	@Override
	public <T extends StructureReadHandle> RowSet<T> resultRows(Plan plan, T rowHandle, Transaction transaction) {
		return resultRows(plan, rowHandle, transaction, ColumnTypes.ROWS);
	}
	@Override
	public <T extends StructureReadHandle> RowSet<T> resultRows(Plan plan, T rowHandle, Transaction transaction, ColumnTypes typeLocation) {
		String rowFormat = getRowFormat(rowHandle);

		RESTServiceResultIterator iter = makeRequest(plan, rowFormat, "inline", transaction, typeLocation);

		return new RowSetHandle<>(rowFormat, iter, rowHandle);
	}
	@Override
    public <T> RowSet<T> resultRowsAs(Plan plan, Class<T> as) {
		return resultRowsAs(plan, as, (Transaction) null, ColumnTypes.ROWS);
	}
	@Override
    public <T> RowSet<T> resultRowsAs(Plan plan, Class<T> as, Transaction transaction) {
		return resultRowsAs(plan, as, transaction, ColumnTypes.ROWS);
	}
	@Override
	public <T> RowSet<T> resultRowsAs(Plan plan, Class<T> as, Transaction transaction, ColumnTypes typeLocation) {
		ContentHandle<T> rowHandle = handleFor(as); 

		String rowFormat = getRowFormat(rowHandle);

		RESTServiceResultIterator iter = makeRequest(plan, rowFormat, "inline", transaction, typeLocation);

		return new RowSetObject<>(rowFormat, iter, rowHandle);
	}

	@Override
	public <T extends StructureReadHandle> T explain(Plan plan, T resultsHandle) {		
		PlanBuilderBase.RequestPlan requestPlan = checkPlan(plan);

		AbstractWriteHandle astHandle = requestPlan.getHandle();

		if (resultsHandle == null) {
			throw new IllegalArgumentException("Must specify a handle to read the explanation for the plan");
		}

		RequestParameters params = new RequestParameters();
		params.add("output", "explain");

		return services.postResource(requestLogger, "rows", null, params, astHandle, resultsHandle);
	}
	@Override
	public <T> T explainAs(Plan plan, Class<T> as) {		
		ContentHandle<T> handle = handleFor(as); 
	    if (explain(plan, (StructureReadHandle) handle) == null) {
	    	return null;
	    }

		return handle.get();
	}

	private <T extends AbstractReadHandle> String getRowFormat(T rowHandle) {
		if (rowHandle == null) {
			throw new IllegalArgumentException("Must specify a handle to iterate over the rows");
		}

		if (!(rowHandle instanceof BaseHandle)) {
			throw new IllegalArgumentException("Cannot iterate rows with invalid handle having class "+rowHandle.getClass().getName());
		}

		BaseHandle<?,?> baseHandle = (BaseHandle<?,?>) rowHandle;

		Format handleFormat = baseHandle.getFormat();
		switch (handleFormat) {
		case JSON:
		case UNKNOWN:
			return "json";
		case XML:
			return "xml";
		default:
			throw new IllegalArgumentException("Must use JSON or XML format to iterate rows instead of "+handleFormat.name());
		}
	}
	private RESTServiceResultIterator makeRequest(
			Plan plan, String rowFormat, String nodeCols, Transaction transaction, ColumnTypes typeLocation
			) {
		PlanBuilderBase.RequestPlan requestPlan = checkPlan(plan);

		AbstractWriteHandle astHandle = requestPlan.getHandle();

		RequestParameters params = getParamBindings(requestPlan);
		params.add("row-format",   rowFormat);
		params.add("node-columns", nodeCols);
		if (ColumnTypes.HEADER == typeLocation) {
			params.add("column-types", "header");
		}

// QUESTION: outputMimetypes a noop?
		return services.postIteratedResource(requestLogger, "rows", transaction, params, astHandle);
	}
	private PlanBuilderBase.RequestPlan checkPlan(Plan plan) {
		if (plan == null) {
			throw new IllegalArgumentException("Must specify a plan to produce row results");
		} else if (!(plan instanceof PlanBuilderBase.RequestPlan)) {
			throw new IllegalArgumentException(
				"Cannot produce rows with invalid plan having class "+plan.getClass().getName()
				);
		}
		return (PlanBuilderBase.RequestPlan) plan;
	}
	private RequestParameters getParamBindings(PlanBuilderBase.RequestPlan requestPlan) {
		RequestParameters params = new RequestParameters();
		Map<PlanBuilderBase.PlanParamBase,XsValueImpl.AnyAtomicTypeValImpl> planParams = requestPlan.getParams();
		if (planParams != null) {
			for (Map.Entry<PlanBuilderBase.PlanParamBase,XsValueImpl.AnyAtomicTypeValImpl> entry: planParams.entrySet()) {
				XsValueImpl.AnyAtomicTypeValImpl val = entry.getValue();
				String datatype = val.getClass().getSimpleName();
				datatype =
						datatype.substring(0, 1).toLowerCase() +
						datatype.substring(1, datatype.length() - "ValImpl".length());
// TODO: add datatype and language qualifications
				params.add("bind:"+entry.getKey().getName(), val.toString());
			}
		}
		return params;
	}

	<T> ContentHandle<T> handleFor(Class<T> as) {
		if (as == null) {
			throw new IllegalArgumentException("Must specify a class for content with a registered handle");
		}

		ContentHandle<T> handle = getHandleRegistry().makeHandle(as);
		if (!(handle instanceof StructureReadHandle)) {
			if (handle == null) {
		    	throw new IllegalArgumentException("Class \"" + as.getName() + "\" has no registered handle");
			} else {
		    	throw new IllegalArgumentException("Class \"" + as.getName() + "\" uses handle " +
						handle.getClass().getName() + " which is not a StructureReadHandle");
			}
	    }

		return handle;
	}

	abstract static class RowSetBase<T> implements RowSet<T>, Iterator<T> {
		String                    rowFormat    = null;
		RESTServiceResultIterator results      = null;
		String[]                  columns      = null;
		RESTServiceResult         nextRow      = null;

		RowSetBase(String rowFormat, RESTServiceResultIterator results) {
			this.rowFormat = rowFormat;
			this.results   = results;
			parseColumns();
			if (results.hasNext()) {
				nextRow = results.next();
			}
		}

		private void parseColumns() {
			if (!results.hasNext()) {
				return;
			}
			RESTServiceResult headerRow = results.next();
			switch(rowFormat) {
			case "json":
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> headerObj = (Map<String, Object>) new ObjectMapper().readValue(
					        headerRow.getContent(new InputStreamHandle()).get(), Map.class
					        );
					if (headerObj != null) {
						@SuppressWarnings("unchecked")
						List<Map<String, String>> cols = (List<Map<String, String>>) headerObj.get("columns");
						int colSize = (cols == null) ? 0 : cols.size();
						if (colSize > 0) {
							columns = new String[colSize];
							int i=0;
							for (Map<String, String> col: cols) {
								columns[i] = col.get("name");
								i++;
							}
						}
					}
				} catch (JsonParseException e) {
					throw new MarkLogicIOException("could not read JSON header part", e);
				} catch (JsonMappingException e) {
					throw new MarkLogicIOException("could not read JSON header map", e);
				} catch (IOException e) {
					throw new MarkLogicIOException("could not read JSON header", e);
				}
				break;
			case "xml":
				try {
					List<String> cols = new ArrayList<>();
					XMLStreamReader headerReader = headerRow.getContent(new XMLStreamReaderHandle()).get();
					while (headerReader.hasNext()) {
						switch(headerReader.next()) {
						case XMLStreamConstants.START_ELEMENT:
							if ("column".equals(headerReader.getLocalName())) {
								cols.add(headerReader.getAttributeValue(null, "name"));
								headerReader.nextTag();
							}
							break;
						}
					}
					int colSize = cols.size();
					if (colSize > 0) {
						columns = cols.toArray(new String[colSize]);
					}
				} catch (XMLStreamException e) {
					throw new MarkLogicIOException("could not read XML header", e);
				}
				break;
			default:
				throw new IllegalArgumentException("Row format should be JSON or XML instead of "+rowFormat);
			}
		}

		@Override
		public String[] getColumnNames() {
			return columns;
		}

		@Override
		public Iterator<T> iterator() {
			return this;
		}
		@Override
		public Stream<T> stream() {
			return StreamSupport.stream(this.spliterator(), false);
		}

		@Override
		public boolean hasNext() {
			return nextRow != null;
		}

		@Override
		public void close() {
			closeImpl();
		}
		@Override
		protected void finalize() throws Throwable {
			closeImpl();
			super.finalize();
		}
		private void closeImpl() {
			if (results != null) {
				results.close();
				results   = null;
				nextRow   = null;
				columns   = null;
			}
		}
	}
	static class RowSetRecord extends RowSetBase<RowRecord> {
		private HandleFactoryRegistry handleRegistry = null;
		RowSetRecord(String rowFormat, RESTServiceResultIterator results, HandleFactoryRegistry handleRegistry) {
			super(rowFormat, results);
			this.handleRegistry = handleRegistry;
		}

		@Override
		public RowRecord next() {
			RESTServiceResult currentRow = nextRow;
			if (currentRow == null) {
				throw new NoSuchElementException("no next row");
			}

			boolean hasMoreRows = results.hasNext();

			try {
				Map<String, RowRecord.ColumnKind> kinds     = new HashMap<>();
				Map<String, String>               datatypes = new HashMap<>();

				// TODO: replace Jackson mapper with binding-sensitive mapper?
				@SuppressWarnings("unchecked")
				Map<String, Object> row = new ObjectMapper().readValue(
						currentRow.getContent(new InputStreamHandle()).get(), Map.class
						);
				row.replaceAll((key, rawBinding) -> {
					@SuppressWarnings("unchecked")
					Map<String,Object> binding = (Map<String,Object>) rawBinding;
					String datatype  = (String) binding.get("type");
					Object value = null;
					if ("cid".equals(datatype)) {
// TODO: increment the count of expected nodes and list the column names expecting values
						kinds.put(key, RowRecord.ColumnKind.CONTENT);
						datatypes.put(key, null);
					} else if ("null".equals(datatype)) {
						kinds.put(key, RowRecord.ColumnKind.NULL);
						datatypes.put(key, null);
					} else if (datatype.contains(":")) {
						kinds.put(key, RowRecord.ColumnKind.ATOMIC_VALUE);
						datatypes.put(key, datatype);
						value = binding.get("value");
					} else {
// TODO: standalone inline nodes such as processing instructions and comments?
throw new MarkLogicInternalException("Column value with unsupported datatype: "+datatype);
					}
					return value;
				});

				
				while (hasMoreRows) {
					currentRow = results.next();

					Map<String,List<String>> headers = currentRow.getHeaders();
					List<String> headerList = headers.get("Content-Disposition");
					if (headerList == null || headerList.isEmpty()) {
						break;
					}
					String headerValue = headerList.get(0);
					if (headerValue == null || !headerValue.startsWith("inline; kind=row-attachment")) {
						break;
					}

					headerList = headers.get("Content-ID");
					if (headerList == null || headerList.isEmpty()) {
						break;
					}
					headerValue = headerList.get(0);
					if (headerValue == null || !(headerValue.startsWith("<") && headerValue.endsWith(">"))) {
						break;
					}
					int pos = headerValue.indexOf("[",1);
					if (pos == -1) {
						break;
					}
					String colName = headerValue.substring(1, pos);

// TODO: check column name
					row.put(colName, currentRow);

					hasMoreRows = results.hasNext();
				}

				RowRecordImpl rowRecord = new RowRecordImpl(handleRegistry);
				
				rowRecord.init(kinds, datatypes, row);

				if (hasMoreRows) {
					nextRow = currentRow;
				} else {
					close();
				}

				return rowRecord;
			} catch (JsonParseException e) {
				throw new MarkLogicIOException("could not part row record", e);
			} catch (JsonMappingException e) {
				throw new MarkLogicIOException("could not map row record", e);
			} catch (IOException e) {
				throw new MarkLogicIOException("could not read row record", e);
			}
		}

		@Override
		public void close() {
			closeImpl();
			super.close();
		}
		@Override
		protected void finalize() throws Throwable {
			closeImpl();
			super.finalize();
		}
		private void closeImpl() {
			if (handleRegistry != null) {
				handleRegistry = null;
			}
		}
	}
	abstract static class RowSetHandleBase<T, R extends AbstractReadHandle> extends RowSetBase<T> {
		private R rowHandle = null;
		RowSetHandleBase(String rowFormat, RESTServiceResultIterator results, R rowHandle) {
			super(rowFormat, results);
			this.rowHandle = rowHandle;
		}

		abstract T makeNextResult(R currentHandle);

// QUESTION: threading guarantees - multiple handles? precedent?
		@Override
		public T next() {
			RESTServiceResult currentRow = nextRow;
			if (currentRow == null) {
				throw new NoSuchElementException("no next row");
			}

			R currentHandle = rowHandle;

			boolean hasMoreRows = results.hasNext();
			if (hasMoreRows) {
				nextRow = results.next();
			} else {
				close();
			}

			return makeNextResult(currentRow.getContent(currentHandle));
		}
		@Override
		public void close() {
			closeImpl();
			super.close();
		}
		@Override
		protected void finalize() throws Throwable {
			closeImpl();
			super.finalize();
		}
		private void closeImpl() {
			if (rowHandle != null) {
				rowHandle = null;
			}
		}
	}
	static class RowSetHandle<T extends StructureReadHandle> extends RowSetHandleBase<T, T> {
		RowSetHandle(String rowFormat, RESTServiceResultIterator results, T rowHandle) {
			super(rowFormat, results, rowHandle);
		}
		@Override
		T makeNextResult(T currentHandle) {
			return currentHandle;
		}
	}
	static class RowSetObject<T> extends RowSetHandleBase<T, ContentHandle<T>> {
		RowSetObject(String rowFormat, RESTServiceResultIterator results, ContentHandle<T> rowHandle) {
			super(rowFormat, results, rowHandle);
		}
		@Override
		T makeNextResult(ContentHandle<T> currentHandle) {
			return currentHandle.get();
		}
	}

	static class RowRecordImpl implements RowRecord {
		private static final
		Map<Class<? extends XsAnyAtomicTypeVal>, Function<String,? extends XsAnyAtomicTypeVal>> factories =
		new HashMap<>();

		private static final Map<Class<? extends XsAnyAtomicTypeVal>,Constructor<?>> constructors = new HashMap<>();

		private Map<String, ColumnKind> kinds     = null;
		private Map<String, String>     datatypes = null;

		private Map<String, Object> row = null;

		private HandleFactoryRegistry handleRegistry = null;

		RowRecordImpl(HandleFactoryRegistry handleRegistry) {
			this.handleRegistry = handleRegistry;
		}

		HandleFactoryRegistry getHandleRegistry() {
			return handleRegistry;
		}

// QUESTION:  threading guarantees - multiple handles? precedent?
		void init(Map<String, ColumnKind> kinds, Map<String, String> datatypes, Map<String, Object> row) {
			this.kinds     = kinds;
			this.datatypes = datatypes;
			this.row       = row;
		}

		@Override
		public ColumnKind getKind(String columnName) {
			if (columnName == null) {
				throw new IllegalArgumentException("cannot get column kind with null name");
			}
			ColumnKind kind = kinds.get(columnName);
			return (kind == null) ? ColumnKind.NULL : kind;
		}

		@Override
		public QName getAtomicDatatype(String columnName) {
			if (columnName == null) {
				throw new IllegalArgumentException("cannot get column datatype with null name");
			}
			String datatype = datatypes.get(columnName);
			if (datatype == null) {
				return null;
			}
			int pos = datatype.indexOf(":");
			if (pos == -1) {
				throw new MarkLogicInternalException("datatype "+datatype+" without prefix");
			}

			String uri = null;
			String prefix = datatype.substring(0, pos);
			switch(prefix) {
			case "rdf":
				uri = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
				break;
			case "sem":
				uri = "http://marklogic.com/semantics";
				break;
			case "xs":
				uri = "http://www.w3.org/2001/XMLSchema";
				break;
			default:
				throw new MarkLogicInternalException("datatype "+datatype+" with unknown prefix: "+prefix);
			}
			return new QName(uri, datatype.substring(pos + 1));
		}

		// supported operations for unmodifiable map
		@Override
		public boolean containsKey(Object key) {
			return row.containsKey(key);
		}
		@Override
		public boolean containsValue(Object value) {
			return row.containsValue(value);
		}
		@Override
		public Set<Entry<String, Object>> entrySet() {
			return row.entrySet();
		}
		@Override
		public Object get(Object key) {
			if (key == null) {
				throw new IllegalArgumentException("cannot get column value with null name");
			}
// TODO: get ColumnKind.NULL as null
// TODO: get ColumnKind.CONTENT of binary as byte[] - getKind()?
// TODO: get ColumnKind.CONTENT if not binary as String - getKind()?
			return row.get(key);
		}
		@Override
		public boolean isEmpty() {
			return row.isEmpty();
		}
		@Override
		public Set<String> keySet() {
			return row.keySet();
		}
		@Override
		public Collection<Object> values() {
			return row.values();
		}
		@Override
		public int size() {
			return row.size();
		}

		// unsupported operations for unmodifiable map
		@Override
		public Object put(String key, Object value) {
			throw new UnsupportedOperationException("cannot modify row record");
		}
		@Override
		public Object remove(Object key) {
			throw new UnsupportedOperationException("cannot modify row record");
		}
		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			throw new UnsupportedOperationException("cannot modify row record");
		}
		@Override
		public void clear() {
			throw new UnsupportedOperationException("cannot modify row record");
		}

		// literal casting convenience getters
		@Override
		public boolean getBoolean(String columnName) {
			return asBoolean(columnName, get(columnName));
		}
		@Override
		public byte getByte(String columnName) {
			return asByte(columnName, get(columnName));
		}
		@Override
		public double getDouble(String columnName) {
			return asDouble(columnName, get(columnName));
		}
		@Override
		public float getFloat(String columnName) {
			return asFloat(columnName, get(columnName));
		}
		@Override
		public int getInt(String columnName) {
			return asInt(columnName, get(columnName));
		}
		@Override
		public long getLong(String columnName) {
			return asLong(columnName, get(columnName));
		}
		@Override
		public short getShort(String columnName) {
			return asShort(columnName, get(columnName));
		}
		@Override
		public String getString(String columnName) {
			return asString(get(columnName));
		}

		private boolean asBoolean(String columnName, Object value) {
			if (value instanceof Boolean) {
				return ((Boolean) value).booleanValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a boolean value");
		}
		private byte asByte(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).byteValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a byte value");
		}
		private double asDouble(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a double value");
		}
		private float asFloat(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).floatValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a float value");
		}
		private int asInt(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).intValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have an integer value");
		}
		private long asLong(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).longValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a long value");
		}
		private short asShort(String columnName, Object value) {
			if (value instanceof Number) {
				return ((Number) value).shortValue();
			}
			throw new IllegalStateException("column "+columnName+" does not have a short value");
		}
		private String asString(Object value) {
			if (value == null || value instanceof String) {
				return (String) value;
			}
			return value.toString();
		}

		private RESTServiceResult getServiceResult(String columnName) {
			Object val = get(columnName);
			if (val instanceof RESTServiceResult) {
				return (RESTServiceResult) val;
			}
			return null;
		}

		@Override
		public <T extends XsAnyAtomicTypeVal> T getValueAs(String columnName, Class<T> as) {
			if (as == null) {
				throw new IllegalArgumentException("cannot construct "+columnName+" value with null class");
			}

			Object value = get(columnName);
			if (value == null) {
				return null;
			}

			/* NOTE: use if refactor away from Jackson ObjectMapper to value construction
			if (as.isInstance(value)) {
				return as.cast(value);
			}
			*/

			String valueStr = asString(value);

			Function<String,? extends XsAnyAtomicTypeVal> factory = getFactory(as);
			if (factory != null) {
				return as.cast(factory.apply(valueStr));
			}

			// fallback
			@SuppressWarnings("unchecked")
			Constructor<T> constructor = (Constructor<T>) constructors.get(as);
			if (constructor == null) {
				try {
					constructor = as.getConstructor(String.class);
				} catch(NoSuchMethodException e) {
					throw new IllegalArgumentException("cannot construct "+columnName+" value as class: "+as.getName());
				}
				constructors.put(as, constructor);
			}

			try {
				return constructor.newInstance(valueStr);
			} catch (InstantiationException e) {
				throw new MarkLogicBindingException("could not construct value as class: "+as.getName(), e);
			} catch (IllegalAccessException e) {
				throw new MarkLogicBindingException("could not construct value as class: "+as.getName(), e);
			} catch (IllegalArgumentException e) {
				throw new MarkLogicBindingException("could not construct value as class: "+as.getName(), e);
			} catch (InvocationTargetException e) {
				throw new MarkLogicBindingException("could not construct value as class: "+as.getName(), e);
			}
		}
		<T extends XsAnyAtomicTypeVal> Function<String,? extends XsAnyAtomicTypeVal> getFactory(Class<T> as) {
			Function<String,? extends XsAnyAtomicTypeVal> factory = factories.get(as);
			if (factory != null) {
				return factory;
			}

			// NOTE: more general first to avoid false fallback
			if (as.isAssignableFrom(XsValueImpl.DecimalValImpl.class)) {
				factory = XsValueImpl.DecimalValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.IntegerValImpl.class)) {
				factory = XsValueImpl.IntegerValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.LongValImpl.class)) {
				factory = XsValueImpl.LongValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.IntValImpl.class)) {
				factory = XsValueImpl.IntValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.ShortValImpl.class)) {
				factory = XsValueImpl.ShortValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.ByteValImpl.class)) {
				factory = XsValueImpl.ByteValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.UnsignedLongValImpl.class)) {
				factory = XsValueImpl.UnsignedLongValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.UnsignedIntValImpl.class)) {
				factory = XsValueImpl.UnsignedIntValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.UnsignedShortValImpl.class)) {
				factory = XsValueImpl.UnsignedShortValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.UnsignedByteValImpl.class)) {
				factory = XsValueImpl.UnsignedByteValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.DoubleValImpl.class)) {
				factory = XsValueImpl.DoubleValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.FloatValImpl.class)) {
				factory = XsValueImpl.FloatValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.DateTimeValImpl.class)) {
				factory = XsValueImpl.DateTimeValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.DateValImpl.class)) {
				factory = XsValueImpl.DateValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.TimeValImpl.class)) {
				factory = XsValueImpl.TimeValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.AnyURIValImpl.class)) {
				factory = XsValueImpl.AnyURIValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.BooleanValImpl.class)) {
				factory = XsValueImpl.BooleanValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.DayTimeDurationValImpl.class)) {
				factory = XsValueImpl.DayTimeDurationValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.GDayValImpl.class)) {
				factory = XsValueImpl.GDayValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.GMonthValImpl.class)) {
				factory = XsValueImpl.GMonthValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.GMonthDayValImpl.class)) {
				factory = XsValueImpl.GMonthDayValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.GYearValImpl.class)) {
				factory = XsValueImpl.GYearValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.GYearMonthValImpl.class)) {
				factory = XsValueImpl.GYearMonthValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.StringValImpl.class)) {
				factory = XsValueImpl.StringValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.YearMonthDurationValImpl.class)) {
				factory = XsValueImpl.YearMonthDurationValImpl::new;
			} else if (as.isAssignableFrom(XsValueImpl.QNameValImpl.class)) {
				factory = XsValueImpl.QNameValImpl::valueOf;
			}

			if (factory != null) {
				factories.put(as,factory);
			}

			return factory;
		}

		@Override
		public Format getContentFormat(String columnName) {
			String mimetype = getContentMimetype(columnName);
			if (mimetype == null) {
				return Format.BINARY;
			}
			switch(mimetype) {
			case "application/json":
				return Format.JSON;
			case "text/plain":
				return Format.TEXT;
			case "application/xml":
			case "application/xml-external-parsed-entity":
				return Format.XML;
			default:
				return Format.BINARY;
			}
		}
		@Override
		public String getContentMimetype(String columnName) {
			if (columnName == null) {
				throw new IllegalArgumentException("cannot get column mime type with null name");
			}
			RESTServiceResult nodeResult = getServiceResult(columnName);
			if (nodeResult == null) {
				return null;
			}
			return nodeResult.getMimetype();
		}
		@Override
		public <T extends AbstractReadHandle> T getContent(String columnName, T contentHandle) {
			if (columnName == null) {
				throw new IllegalArgumentException("cannot get column node with null name");
			}
			RESTServiceResult nodeResult = getServiceResult(columnName);
			if (nodeResult == null) {
				return null;
			}
			return nodeResult.getContent(contentHandle);
		}
		@Override
		public <T> T getContentAs(String columnName, Class<T> as) {
			if (as == null) {
				throw new IllegalArgumentException("Must specify a class for content with a registered handle");
			}

			ContentHandle<T> handle = getHandleRegistry().makeHandle(as);
			if (handle == null) {
				throw new IllegalArgumentException("No handle registered for class: "+as.getName());
			}

			handle = getContent(columnName, handle);

			T content = (handle == null) ? null : handle.get();

			return content;
		}
	}
	static class RawPlanDefinitionImpl implements RawPlanDefinition, PlanBuilderBase.RequestPlan {
		private Map<PlanBuilderBase.PlanParamBase,XsValueImpl.AnyAtomicTypeValImpl> params = null;
		private JSONWriteHandle handle = null;
		RawPlanDefinitionImpl(JSONWriteHandle handle) {
			setHandle(handle);
		}

		@Override
		public Map<PlanBuilderBase.PlanParamBase,XsValueImpl.AnyAtomicTypeValImpl> getParams() {
	    	return params;
	    }

	    @Override
	    public Plan bindParam(PlanParam param, boolean literal) {
	    	return bindParam(param, new XsValueImpl.BooleanValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, byte literal) {
	    	return bindParam(param, new XsValueImpl.ByteValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, double literal) {
	    	return bindParam(param, new XsValueImpl.DoubleValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, float literal) {
	    	return bindParam(param, new XsValueImpl.FloatValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, int literal) {
	    	return bindParam(param, new XsValueImpl.IntValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, long literal) {
	    	return bindParam(param, new XsValueImpl.LongValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, short literal) {
	    	return bindParam(param, new XsValueImpl.ShortValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, String literal) {
	    	return bindParam(param, new XsValueImpl.StringValImpl(literal));
	    }
	    @Override
	    public Plan bindParam(PlanParam param, XsAnyAtomicTypeVal literal) {
	    	if (!(param instanceof PlanBuilderBase.PlanParamBase)) {
	    		throw new IllegalArgumentException("cannot set parameter that doesn't extend base");
	    	}
	    	if (!(literal instanceof XsValueImpl.AnyAtomicTypeValImpl)) {
	    		throw new IllegalArgumentException("cannot set value with unknown implementation");
	    	}
	    	if (params == null) {
	    		params = new HashMap<>();
	    	}
	    	params.put((PlanBuilderBase.PlanParamBase) param, (XsValueImpl.AnyAtomicTypeValImpl) literal);
// TODO: return clone with param for immutability
	    	return this;
		}

		@Override
		public JSONWriteHandle getHandle() {
			return handle;
		}
		@Override
		public void setHandle(JSONWriteHandle handle) {
			if (handle == null) {
				throw new IllegalArgumentException("Must specify handle for reading raw plan");
			}
			this.handle = handle;
		}
		@Override
		public RawPlanDefinition withHandle(JSONWriteHandle handle) {
			setHandle(handle);
			return this;
		}
	}
}