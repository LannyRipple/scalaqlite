class Sqlite(path: String) {
    private var db = Array(0L)
    assertSuccess("open", Sqlite3C.open(path, db))
    private def assertSuccess(method: String, rc: Int): Unit =
        if (rc != Sqlite3C.OK)
            throw new Exception(method + " failed: " + Sqlite3C.errmsg(db(0)))
    private def assertOpen: Unit = if (db(0) == 0) throw new Exception("db is not open")
    def close(): Unit = {
        assertOpen
        assertSuccess("close", Sqlite3C.close(db(0)))
        db(0) = 0
    }
    override def finalize(): Unit = if (db(0) != 0) Sqlite3C.close(db(0))

    class Result(dbc: Sqlite, stmtc: Long) {
        private val db: Sqlite = dbc
        private var stmt: Long = stmtc
        def done: Boolean = stmt == 0
        private def assertNotDone: Unit = if (done) throw new Exception("already done")
        def next: Unit = {
            assertNotDone
            var r = Sqlite3C.step(stmt)
            if (r == Sqlite3C.DONE) {
                Sqlite3C.finalize(stmt)
                stmt = 0
            }
            else if (r != Sqlite3C.ROW) {
                throw new Exception("unexpected result: " + r)
            }
        }
        override def finalize: Unit = if (stmt != 0) Sqlite3C.finalize(stmt)
        def columnCount(): Int = {
            assertNotDone
            Sqlite3C.column_count(stmt)
        }
        def columnType(i: Int): Int = {
            assertNotDone
            Sqlite3C.column_type(stmt, i)
        }
        def asInt(i: Int): Int = {
            assertNotDone
            Sqlite3C.column_int(stmt, i)
        }
        def asDouble(i: Int): Double = {
            assertNotDone
            Sqlite3C.column_double(stmt, i)
        }
        def asText(i: Int): String = {
            assertNotDone
            Sqlite3C.column_text(stmt, i)
        }
        class Row(resc: Result) {
            private val res = resc
            class Element(resc: Result, ic: Int) {
                private val res = resc
                private val i = ic
                def typ: Int = res.columnType(i)
                def asInt: Int = res.asInt(i)
                def asDouble: Double = res.asDouble(i)
                def asText: String = res.asText(i)
                override def toString(): String = {
                    typ match {
                        case Sqlite3C.INTEGER => asInt.toString
                        case Sqlite3C.FLOAT => asDouble.toString
                        case Sqlite3C.TEXT => asText
                        case Sqlite3C.NULL => "NULL"
                        case _ => "???"
                    }
                }
            }
            def elt(i: Int): Element = new Element(res, i)
            def foreach(f: Element => Unit): Unit =
                for (i <- 0 until res.columnCount())
                    f(elt(i))
            def map[T](f: Element => T): IndexedSeq[T] =
              for (i <- 0 until res.columnCount())
                  yield f(elt(i))
        }
        def foreach(f: Row => Unit): Unit =
            while (!done) {
                f(new Row(this))
                next
            }
    }
    def query(sql: String): Result = {
        assertOpen
        var stmt = Array(0L)
        val rc = Sqlite3C.prepare_v2(db(0), sql, stmt)
        assertSuccess("prepare", rc)
        var res = new Result(this, stmt(0))
        res.next
        res
    }
    def execute(sql: String): Unit = {
        var stmt = query(sql)
        while (!stmt.done)
            stmt.next
    }
}
