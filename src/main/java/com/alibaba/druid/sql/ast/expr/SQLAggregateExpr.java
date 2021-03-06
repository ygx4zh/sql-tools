/*
 * Copyright 1999-2017 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql.ast.expr;

import com.alibaba.druid.sql.FnvHash;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SQLAggregateExpr extends SQLExprImpl implements Serializable, SQLReplaceable {

    private static final long     serialVersionUID = 1L;

    protected String              methodName;
    protected long                methodNameHashCod64;

    protected SQLAggregateOption  option;
    protected final List<SQLExpr> arguments        = new ArrayList<SQLExpr>();
    protected SQLKeep keep;
    protected SQLOver             over;
    protected SQLOrderBy          withinGroup;
    protected boolean             ignoreNulls      = false;

    public SQLAggregateExpr(String methodName){
        this.methodName = methodName;
    }

    public SQLAggregateExpr(String methodName, SQLAggregateOption option){
        this.methodName = methodName;
        this.option = option;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long methodNameHashCod64() {
        if (methodNameHashCod64 == 0) {
            methodNameHashCod64 = FnvHash.hashCode64(methodName);
        }
        return methodNameHashCod64;
    }

    public SQLOrderBy getWithinGroup() {
        return withinGroup;
    }

    public void setWithinGroup(SQLOrderBy withinGroup) {
        if (withinGroup != null) {
            withinGroup.setParent(this);
        }

        this.withinGroup = withinGroup;
    }

    public SQLAggregateOption getOption() {
        return this.option;
    }

    public void setOption(SQLAggregateOption option) {
        this.option = option;
    }

    public List<SQLExpr> getArguments() {
        return this.arguments;
    }
    
    public void addArgument(SQLExpr argument) {
        if (argument != null) {
            argument.setParent(this);
        }
        this.arguments.add(argument);
    }

    public SQLOver getOver() {
        return over;
    }

    public void setOver(SQLOver over) {
        if (over != null) {
            over.setParent(this);
        }
        this.over = over;
    }
    
    public SQLKeep getKeep() {
        return keep;
    }

    public void setKeep(SQLKeep keep) {
        if (keep != null) {
            keep.setParent(this);
        }
        this.keep = keep;
    }
    
    public boolean isIgnoreNulls() {
        return this.ignoreNulls;
    }

    public void setIgnoreNulls(boolean ignoreNulls) {
        this.ignoreNulls = ignoreNulls;
    }

    public String toString() {
        return SQLUtils.toSQLString(this);
    }


    @Override
    protected void accept0(SQLASTVisitor visitor) {
        if (visitor.visit(this)) {
            acceptChild(visitor, this.arguments);
            acceptChild(visitor, this.keep);
            acceptChild(visitor, this.over);
            acceptChild(visitor, this.withinGroup);
        }

        visitor.endVisit(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((option == null) ? 0 : option.hashCode());
        result = prime * result + ((over == null) ? 0 : over.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SQLAggregateExpr other = (SQLAggregateExpr) obj;
        if (arguments == null) {
            if (other.arguments != null) {
                return false;
            }
        } else if (!arguments.equals(other.arguments)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        if (over == null) {
            if (other.over != null) {
                return false;
            }
        } else if (!over.equals(other.over)) {
            return false;
        }
        if (option != other.option) {
            return false;
        }
        return true;
    }

    public SQLAggregateExpr clone() {
        SQLAggregateExpr x = new SQLAggregateExpr(methodName);

        x.option = option;

        for (SQLExpr arg : arguments) {
            x.addArgument(arg.clone());
        }

        if (keep != null) {
            x.setKeep(keep.clone());
        }

        if (over != null) {
            x.setOver(over.clone());
        }

        if (withinGroup != null) {
            x.setWithinGroup(withinGroup.clone());
        }

        x.ignoreNulls = ignoreNulls;

        return x;
    }

    public SQLDataType computeDataType() {
        long hash = methodNameHashCod64();

        if (hash == FnvHash.Constants.COUNT
                || hash == FnvHash.Constants.ROW_NUMBER) {
            return SQLIntegerExpr.DEFAULT_DATA_TYPE;
        }

        if (arguments.size() > 0) {
            SQLDataType dataType = arguments.get(0).computeDataType();
            if (dataType != null) {
                return dataType;
            }
        }

        if (hash == FnvHash.Constants.WM_CONAT
                || hash == FnvHash.Constants.GROUP_CONCAT) {
            return SQLCharExpr.DEFAULT_DATA_TYPE;
        }

        return null;
    }

    public boolean replace(SQLExpr expr, SQLExpr target) {
        if (target == null) {
            return false;
        }
        for (int i = 0; i < arguments.size(); ++i) {
            if (arguments.get(i) == expr) {
                arguments.set(i, target);
                target.setParent(this);
                return true;
            }
        }
        return false;
    }
}
