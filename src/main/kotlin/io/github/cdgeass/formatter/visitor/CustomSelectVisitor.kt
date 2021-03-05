package io.github.cdgeass.formatter.visitor

import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.values.ValuesStatement

/**
 * @author cdgeass
 * @since  2021-03-05
 */
class CustomSelectVisitor(level: Int) : AbstractCustomVisitor(level), SelectVisitor {

    override fun visit(plainSelect: PlainSelect?) {
        val plainSelect = plainSelect ?: return

        if (plainSelect.isUseBrackets) {
            append("(")
        }
        appendTab().append("SELECT ")

        if (plainSelect.oracleHint != null) {
            append(plainSelect.oracleHint.toString()).append(" ")
        }

        if (plainSelect.skip != null) {
            append(plainSelect.skip.toString()).append(" ")
        }

        if (plainSelect.first != null) {
            append(plainSelect.first.toString()).append(" ")
        }

        if (plainSelect.distinct != null) {
            append(plainSelect.distinct.toString()).append(" ")
        }
        if (plainSelect.top != null) {
            append(plainSelect.top.toString()).append(" ")
        }
        if (plainSelect.mySqlSqlNoCache) {
            append("SQL_NO_CACHE").append(" ")
        }
        if (plainSelect.mySqlSqlCalcFoundRows) {
            append("SQL_CALC_FOUND_ROWS").append(" ")
        }
        append(getStringList(plainSelect.selectItems, true, false, true, nextLevel()))

        if (plainSelect.intoTables != null) {
            append(" INTO ").append(plainSelect.intoTables.joinToString(",") { toString() })
        }

        if (plainSelect.fromItem != null) {
            appendTab().append("FROM ")
                .append(CustomFromItemVisitor(nextLevel()).apply {
                    plainSelect.fromItem.accept(this)
                }.toString())
            if (plainSelect.joins != null) {
                plainSelect.joins.forEach {
                    if (it.isSimple) {
                        append(
                            ", " + join(
                                it,
                                currentLevel()
                            )
                        )
                    } else {
                        append(" " + join(it, currentLevel()))
                    }
                }
            }

            if (plainSelect.ksqlWindow != null) {
                appendTab().append("WINDOW ").append(plainSelect.ksqlWindow.toString())
            }
            if (plainSelect.where != null) {
                appendTab().append("WHERE ").append(expression(plainSelect.where, currentLevel()))
            }
            if (plainSelect.oracleHierarchical != null) {
                append(plainSelect.oracleHierarchical.toString())
            }
            if (plainSelect.groupBy != null) {
                appendTab().append(plainSelect.groupBy.toString())
            }
            if (plainSelect.having != null) {
                appendTab().append("HAVING ").append(plainSelect.having.toString())
            }
            if (plainSelect.orderByElements.isNotEmpty()) {
                appendTab().append(
                    orderByToString(
                        plainSelect.isOracleSiblings,
                        plainSelect.orderByElements
                    )
                )
            }
            if (plainSelect.limit != null) {
                appendTab().append(plainSelect.limit.toString().trim())
            }
            if (plainSelect.offset != null) {
                if (plainSelect.limit == null) {
                    appendTab()
                }
                append(plainSelect.offset.toString().trim())
            }
            if (plainSelect.fetch != null) {
                appendTab().append(plainSelect.fetch.toString())
            }
            if (plainSelect.isForUpdate) {
                appendTab().append("FOR UPDATE")
                if (plainSelect.forUpdateTable != null) {
                    append(" OF ").append(plainSelect.forUpdateTable.toString())
                }
                if (plainSelect.wait != null) {
                    append(plainSelect.wait.toString())
                }
            }
            if (plainSelect.optimizeFor != null) {
                appendTab().append(plainSelect.optimizeFor.toString())
            }
        } else {
            if (plainSelect.where != null) {
                appendTab().append("WHERE ").append(plainSelect.where.toString())
            }
        }
        if (plainSelect.forXmlPath != null) {
            appendTab().append("FOR XML PATH(").append(plainSelect.forXmlPath).append(")")
        }
        if (plainSelect.isUseBrackets) {
            appendPreTab().append(")")
        }
    }

    override fun visit(setOpList: SetOperationList?) {
        val setOpList = setOpList ?: return

        setOpList.selects.forEachIndexed { index: Int, select: SelectBody ->
            if (index != 0) {
                appendTab().append(setOpList.operations[index - 1].toString()).append(" ")
            }
            val customSelectVisitor = CustomSelectVisitor(currentLevel()).apply {
                select.accept(this)
            }
            if (setOpList.brackets == null || setOpList.brackets[index]) {
                append("(").append(customSelectVisitor.toString()).append(")")
            } else {
                append(customSelectVisitor.toString())
            }
        }

        if (setOpList.orderByElements != null) {
            appendTab().append(PlainSelect.orderByToString(setOpList.orderByElements))
        }
        if (setOpList.limit != null) {
            appendTab().append(setOpList.limit.toString().trim())
        }
        if (setOpList.offset != null) {
            if (setOpList.limit == null) {
                appendTab()
            }
            append(setOpList.offset.toString().trim())
        }
        if (setOpList.fetch != null) {
            appendTab().append(setOpList.fetch.toString())
        }
    }

    override fun visit(withItem: WithItem?) {
        val withItem = withItem ?: return

        appendTab()
        if (withItem.isRecursive) {
            append("RECURSIVE ")
        }

        append(withItem.name)

        if (withItem.withItemList.isNotEmpty()) {
            append(getStringList(withItem.withItemList, true, true))
        }

        appendTab().append("AS (")
        append(CustomSelectVisitor(nextLevel()).apply { withItem.selectBody.accept(this) }.toString())
        appendTab().append(")")
    }

    override fun visit(aThis: ValuesStatement?) {
        val aThis = aThis ?: return

        appendTab().append("VALUES ").append(getStringList(aThis.expressions, true, true))
    }
}