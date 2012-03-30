//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-b10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.03.20 at 02:46:45 PM PDT 
//


package com.marklogic.client.config.search.jaxb;

import java.util.List;
import java.util.Vector;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;choice>
 *           &lt;choice>
 *             &lt;element ref="{http://marklogic.com/appservices/search}element"/>
 *             &lt;element ref="{http://marklogic.com/appservices/search}attribute"/>
 *             &lt;element ref="{http://marklogic.com/appservices/search}fragment-scope"/>
 *           &lt;/choice>
 *           &lt;element ref="{http://marklogic.com/appservices/search}field"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://marklogic.com/appservices/search}anchor"/>
 *         &lt;element ref="{http://marklogic.com/appservices/search}bucket"/>
 *         &lt;element ref="{http://marklogic.com/appservices/search}computed-bucket"/>
 *         &lt;element ref="{http://marklogic.com/appservices/search}facet-option"/>
 *       &lt;/choice>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="collation" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;attribute name="facet" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@XmlType(name = "", propOrder = {
    "elementOrAttributeOrFragmentScope"
})
public class Range {

    @XmlElementRefs({
        @XmlElementRef(name = "bucket", namespace = "http://marklogic.com/appservices/search", type = Bucket.class),
        @XmlElementRef(name = "element", namespace = "http://marklogic.com/appservices/search", type = Element.class),
        @XmlElementRef(name = "field", namespace = "http://marklogic.com/appservices/search", type = Field.class),
        @XmlElementRef(name = "attribute", namespace = "http://marklogic.com/appservices/search", type = Attribute.class),
        @XmlElementRef(name = "facet-option", namespace = "http://marklogic.com/appservices/search", type = JAXBElement.class),
        @XmlElementRef(name = "anchor", namespace = "http://marklogic.com/appservices/search", type = Anchor.class),
        @XmlElementRef(name = "fragment-scope", namespace = "http://marklogic.com/appservices/search", type = JAXBElement.class),
        @XmlElementRef(name = "computed-bucket", namespace = "http://marklogic.com/appservices/search", type = ComputedBucket.class)
    })
    protected List<Object> elementOrAttributeOrFragmentScope = new Vector<Object>();
    @XmlAttribute(name = "type", required = true)
    protected QName type;
    @XmlAttribute(name = "collation")
    @XmlSchemaType(name = "anyURI")
    protected String collation;
    @XmlAttribute(name = "facet")
    protected Boolean facet;

    /**
     * Gets the value of the elementOrAttributeOrFragmentScope property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elementOrAttributeOrFragmentScope property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElementOrAttributeOrFragmentScope().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BucketImpl }
     * {@link Element }
     * {@link Field }
     * {@link Attribute }
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link Anchor }
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link ComputedBucket }
     * 
     * 
     */
    public List<Object> getElementOrAttributeOrFragmentScope() {
        if (elementOrAttributeOrFragmentScope == null) {
            elementOrAttributeOrFragmentScope = new Vector<Object>();
        }
        return this.elementOrAttributeOrFragmentScope;
    }

    public boolean isSetElementOrAttributeOrFragmentScope() {
        return ((this.elementOrAttributeOrFragmentScope!= null)&&(!this.elementOrAttributeOrFragmentScope.isEmpty()));
    }

    public void unsetElementOrAttributeOrFragmentScope() {
        this.elementOrAttributeOrFragmentScope = null;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setType(QName value) {
        this.type = value;
    }

    public boolean isSetType() {
        return (this.type!= null);
    }

    /**
     * Gets the value of the collation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollation() {
        return collation;
    }

    /**
     * Sets the value of the collation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollation(String value) {
        this.collation = value;
    }

    public boolean isSetCollation() {
        return (this.collation!= null);
    }

    /**
     * Gets the value of the facet property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isFacet() {
        return facet;
    }

    /**
     * Sets the value of the facet property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFacet(boolean value) {
        this.facet = value;
    }

    public boolean isSetFacet() {
        return (this.facet!= null);
    }

    public void unsetFacet() {
        this.facet = null;
    }

}
