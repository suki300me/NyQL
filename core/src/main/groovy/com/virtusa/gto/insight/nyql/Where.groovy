package com.virtusa.gto.insight.nyql

import com.virtusa.gto.insight.nyql.exceptions.NySyntaxException
import com.virtusa.gto.insight.nyql.model.QScript
import com.virtusa.gto.insight.nyql.model.units.AParam
import com.virtusa.gto.insight.nyql.model.units.ParamList
import com.virtusa.gto.insight.nyql.traits.DataTypeTraits
import com.virtusa.gto.insight.nyql.traits.FunctionTraits
import com.virtusa.gto.insight.nyql.traits.ScriptTraits
import com.virtusa.gto.insight.nyql.utils.Constants
import com.virtusa.gto.insight.nyql.utils.QOperator
import com.virtusa.gto.insight.nyql.utils.QUtils
import com.virtusa.gto.insight.nyql.utils.QueryType

/**
 * @author Isuru Weerarathna
 */
class Where implements DataTypeTraits, FunctionTraits, ScriptTraits {

    QContext _ctx = null

    List<Object> clauses = new ArrayList<>()

    Where(QContext context) {
        _ctx = context
    }

    def ALL(@DelegatesTo(Where) closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: 'AND'))
        return this
    }

    def ANY(closure) {
        Where inner = new Where(_ctx)
        def code = closure.rehydrate(inner, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        clauses.add(new QConditionGroup(where: inner, condConnector: 'OR'))
        return this
    }

    def LIKE(Object c1, Object c2) {
        return ON(c1, LIKE(c2))
    }

    def NOTLIKE(Object c1, Object c2) {
        return ON(c1, NOTLIKE(c2))
    }

    AParam PARAM(String name, AParam.ParamScope scope=null, String mappingName=null) {
        return _ctx.addParam(QUtils.createParam(name, scope, mappingName))
    }

    AParam PARAMLIST(String name) {
        return _ctx.addParam(new ParamList(__name: name))
    }

    def AND(Closure closure) {
        AND()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return this
    }

    def OR(Closure closure) {
        OR()

        def code = closure.rehydrate(this, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return this
    }

    def OR() {
        clauses.add(' OR ')
        return this
    }

    def AND() {
        clauses.add(' AND ')
        return this
    }

    def ON(Object c1, QOperator op = QOperator.UNKNOWN, Object c2) {
        clauses.add(new QCondition(leftOp: c1, rightOp: c2, op: op))
        return this
    }

    def ISNULL(Object c) {
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: QOperator.IS))
        return this
    }

    def NOTNULL(Object c) {
        clauses.add(new QCondition(leftOp: c, rightOp: null, op: QOperator.IS_NOT))
        return this
    }

    def EQ(Object c1, Object c2) {
        if (c2 == null) {
            return ISNULL(c1)
        }
        return ON(c1, QOperator.EQUAL, c2)
    }

    def NEQ(Object c1, Object c2) {
        if (c2 == null) {
            return NOTNULL(c1)
        }
        return ON(c1, QOperator.NOT_EQUAL, c2)
    }

    def GT(Object c1, Object c2) {
        return ON(c1, QOperator.GREATER_THAN, c2)
    }

    def GTE(Object c1, Object c2) {
        return ON(c1, QOperator.GREATER_THAN_EQUAL, c2)
    }

    def LT(Object c1, Object c2) {
        return ON(c1, QOperator.LESS_THAN, c2)
    }

    def LTE(Object c1, Object c2) {
        return ON(c1, QOperator.LESS_THAN_EQUAL, c2)
    }

    def BETWEEN(Object c1, Object startValue, Object endValue) {
        return ON(c1, BETWEEN(startValue, endValue))
    }

    def NOTBETWEEN(Object c1, Object startValue, Object endValue) {
        return ON(c1, NOT_BETWEEN(startValue, endValue))
    }

    def IN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.size() == 0) {
                list.add(null)
            }
            return ON(c1, QOperator.IN, list)
        }
    }

    def NOTIN(Object c1, Object... cs) {
        if (cs != null) {
            List list = new LinkedList()
            QUtils.expandToList(list, cs)
            if (list.isEmpty()) {
                list.add(null)
            }
            return ON(c1, QOperator.NOT_IN, list)
        }
    }

    def $IMPORT(String scriptId) {
        QScript script = _ctx.ownerSession.scriptRepo.parse(scriptId, _ctx.ownerSession)
        QResultProxy proxy = script.proxy
        if (proxy.queryType == QueryType.PART && proxy.rawObject instanceof Where) {
            Query q = proxy.qObject as Query
            Where inner = proxy.rawObject
            clauses.addAll(inner.clauses)
            _ctx.mergeFrom(q._ctx)
            return this
        } else {
            return proxy
        }
    }

    def CASE(closure) {
        Case aCase = new Case(_ctx: _ctx, _ownerQ: _ctx.ownQuery)

        def code = closure.rehydrate(aCase, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        return aCase
    }

    def IFNULL(Column column, Object val) {
        Case aCase = CASE({
            WHEN { ISNULL(column) }
            THEN { val }
            ELSE { column }
        })
        aCase.setCaseType(Case.CaseType.IFNULL)
        return aCase
    }

    def IFNOTNULL(Column column, Object val) {
        return CASE({
            WHEN { NOTNULL(column) }
            THEN { val }
            ELSE { column }
        })
    }

    def EXISTS(Object subQuery) {
        clauses.add(new QUnaryCondition(rightOp: subQuery, op: QOperator.EXISTS))
        return this
    }

    def NOTEXISTS(Object subQuery) {
        clauses.add(new QUnaryCondition(rightOp: subQuery, op: QOperator.NOT_EXISTS))
        return this
    }

    def RAW(Object val) {
        clauses.add(val)
        return this
    }

    def __hasClauses() {
        return QUtils.notNullNorEmpty(clauses)
    }

    def propertyMissing(String name) {
        if ('AND' == name) {
            return AND()
        } else if ('OR' == name) {
            return OR()
        } else if (name == Constants.DSL_SESSION_WORD) {
            return _ctx.ownerSession.sessionVariables
        }

        if (_ctx.tables.containsKey(name)) {
            return _ctx.tables[name]
        } else {
            if (_ctx.columns.containsKey(name)) {
                return _ctx.columns.get(name)
            }

            def column = _ctx.getTheOnlyTable()?.COLUMN(name)
            if (column != null) {
                return column
            }
            throw new NySyntaxException("No table by name '$name' found!")
        }
    }

    def methodMissing(String name, def args) {
        if (name == 'AND' || name == 'OR') {
            if (args.getClass().isArray() && args[0] instanceof Where) {
                ((Where) args[0]).appendOneLastBefore(name + ' ');
                return
            }
        }
        throw new NySyntaxException("Unknown function detected! [Name: '$name', params: $args]")
    }

    protected void appendOneLastBefore(String clause) {
        if (clauses.size() > 0) {
            int idx = clauses.size() - 1
            clauses.add(idx, ' ' + clause)
        } else {
            clauses.add(clause)
        }
    }

    static class QConditionGroup {
        Where where
        String condConnector = 'AND'
    }

    static class QCondition {
        def leftOp
        def rightOp
        QOperator op
    }

    static class QUnaryCondition extends QCondition {
        def chooseOp() {
            return leftOp ?: rightOp
        }
    }

}
