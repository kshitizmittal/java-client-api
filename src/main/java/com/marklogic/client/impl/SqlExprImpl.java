/*
 * Copyright 2016-2017 MarkLogic Corporation
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

import com.marklogic.client.type.ItemExpr;
import com.marklogic.client.type.ItemSeqExpr;
import com.marklogic.client.type.XsDecimalExpr;
import com.marklogic.client.type.XsIntegerExpr;
import com.marklogic.client.type.XsIntExpr;
import com.marklogic.client.type.XsNumericExpr;
import com.marklogic.client.type.XsStringExpr;
import com.marklogic.client.type.XsUnsignedIntExpr;
import com.marklogic.client.type.XsUnsignedLongExpr;



import com.marklogic.client.expression.SqlExpr;
import com.marklogic.client.impl.BaseTypeImpl;

// IMPORTANT: Do not edit. This file is generated.
class SqlExprImpl implements SqlExpr {

    final static XsExprImpl xs = XsExprImpl.xs;

    final static SqlExprImpl sql = new SqlExprImpl();

    SqlExprImpl() {
    }

    
    @Override
    public XsIntegerExpr bitLength(XsStringExpr str) {
        return new XsExprImpl.IntegerCallImpl("sql", "bit-length", new Object[]{ str });
    }

    
    @Override
    public ItemExpr dateadd(XsStringExpr datepart, int number, ItemExpr date) {
        return dateadd(datepart, xs.intVal(number), date);
    }

    
    @Override
    public ItemExpr dateadd(XsStringExpr datepart, XsIntExpr number, ItemExpr date) {
        return new BaseTypeImpl.ItemCallImpl("sql", "dateadd", new Object[]{ datepart, number, date });
    }

    
    @Override
    public XsIntegerExpr datediff(XsStringExpr datepart, ItemExpr startdate, ItemExpr enddate) {
        return new XsExprImpl.IntegerCallImpl("sql", "datediff", new Object[]{ datepart, startdate, enddate });
    }

    
    @Override
    public XsIntegerExpr datepart(XsStringExpr datepart, ItemExpr date) {
        return new XsExprImpl.IntegerCallImpl("sql", "datepart", new Object[]{ datepart, date });
    }

    
    @Override
    public XsIntegerExpr day(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "day", new Object[]{ arg1 });
    }

    
    @Override
    public XsStringExpr dayname(ItemExpr arg1) {
        return new XsExprImpl.StringCallImpl("sql", "dayname", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr hours(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "hours", new Object[]{ arg1 });
    }

    
    @Override
    public XsStringExpr insert(XsStringExpr str, double start, double length, String str2) {
        return insert(str, xs.doubleVal(start), xs.doubleVal(length), (str2 == null) ? (XsStringExpr) null : xs.string(str2));
    }

    
    @Override
    public XsStringExpr insert(XsStringExpr str, XsNumericExpr start, XsNumericExpr length, XsStringExpr str2) {
        return new XsExprImpl.StringCallImpl("sql", "insert", new Object[]{ str, start, length, str2 });
    }

    
    @Override
    public XsUnsignedIntExpr instr(XsStringExpr str, String n) {
        return instr(str, (n == null) ? (XsStringExpr) null : xs.string(n));
    }

    
    @Override
    public XsUnsignedIntExpr instr(XsStringExpr str, XsStringExpr n) {
        return new XsExprImpl.UnsignedIntCallImpl("sql", "instr", new Object[]{ str, n });
    }

    
    @Override
    public XsStringExpr left(ItemSeqExpr str, double n) {
        return left(str, xs.doubleVal(n));
    }

    
    @Override
    public XsStringExpr left(ItemSeqExpr str, XsNumericExpr n) {
        return new XsExprImpl.StringCallImpl("sql", "left", new Object[]{ str, n });
    }

    
    @Override
    public XsStringExpr ltrim(XsStringExpr str) {
        return new XsExprImpl.StringCallImpl("sql", "ltrim", new Object[]{ str });
    }

    
    @Override
    public XsIntegerExpr minutes(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "minutes", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr month(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "month", new Object[]{ arg1 });
    }

    
    @Override
    public XsStringExpr monthname(ItemExpr arg1) {
        return new XsExprImpl.StringCallImpl("sql", "monthname", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr octetLength(XsStringExpr x) {
        return new XsExprImpl.IntegerCallImpl("sql", "octet-length", new Object[]{ x });
    }

    
    @Override
    public XsIntegerExpr quarter(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "quarter", new Object[]{ arg1 });
    }

    
    @Override
    public XsUnsignedLongExpr rand(XsUnsignedLongExpr n) {
        return new XsExprImpl.UnsignedLongCallImpl("sql", "rand", new Object[]{ n });
    }

    
    @Override
    public XsStringExpr repeat(ItemSeqExpr str, double n) {
        return repeat(str, xs.doubleVal(n));
    }

    
    @Override
    public XsStringExpr repeat(ItemSeqExpr str, XsNumericExpr n) {
        return new XsExprImpl.StringCallImpl("sql", "repeat", new Object[]{ str, n });
    }

    
    @Override
    public XsStringExpr right(ItemSeqExpr str, double n) {
        return right(str, xs.doubleVal(n));
    }

    
    @Override
    public XsStringExpr right(ItemSeqExpr str, XsNumericExpr n) {
        return new XsExprImpl.StringCallImpl("sql", "right", new Object[]{ str, n });
    }

    
    @Override
    public XsStringExpr rtrim(XsStringExpr str) {
        return new XsExprImpl.StringCallImpl("sql", "rtrim", new Object[]{ str });
    }

    
    @Override
    public XsDecimalExpr seconds(ItemExpr arg1) {
        return new XsExprImpl.DecimalCallImpl("sql", "seconds", new Object[]{ arg1 });
    }

    
    @Override
    public ItemSeqExpr sign(XsNumericExpr x) {
        return new BaseTypeImpl.ItemSeqCallImpl("sql", "sign", new Object[]{ x });
    }

    
    @Override
    public XsStringExpr space(XsNumericExpr n) {
        return new XsExprImpl.StringCallImpl("sql", "space", new Object[]{ n });
    }

    
    @Override
    public ItemExpr timestampadd(XsStringExpr arg1, int arg2, ItemExpr arg3) {
        return timestampadd(arg1, xs.intVal(arg2), arg3);
    }

    
    @Override
    public ItemExpr timestampadd(XsStringExpr arg1, XsIntExpr arg2, ItemExpr arg3) {
        return new BaseTypeImpl.ItemCallImpl("sql", "timestampadd", new Object[]{ arg1, arg2, arg3 });
    }

    
    @Override
    public XsIntegerExpr timestampdiff(XsStringExpr arg1, ItemExpr arg2, ItemExpr arg3) {
        return new XsExprImpl.IntegerCallImpl("sql", "timestampdiff", new Object[]{ arg1, arg2, arg3 });
    }

    
    @Override
    public XsStringExpr trim(XsStringExpr str) {
        return new XsExprImpl.StringCallImpl("sql", "trim", new Object[]{ str });
    }

    
    @Override
    public XsIntegerExpr week(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "week", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr weekday(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "weekday", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr year(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "year", new Object[]{ arg1 });
    }

    
    @Override
    public XsIntegerExpr yearday(ItemExpr arg1) {
        return new XsExprImpl.IntegerCallImpl("sql", "yearday", new Object[]{ arg1 });
    }

    }