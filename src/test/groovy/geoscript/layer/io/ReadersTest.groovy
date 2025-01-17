package geoscript.layer.io

import org.junit.jupiter.api.Test
import static org.junit.jupiter.api.Assertions.*

/**
 * The Layer Readers Unit Test.
 * @author Jared Erickson
 */
class ReadersTest {

    @Test void list() {
        List<Reader> readers = Readers.list()
        assertNotNull readers
        assertTrue readers.size() > 0
    }

    @Test void find() {
        Reader reader = Readers.find("csv")
        assertNotNull reader

        reader = Readers.find("asdf")
        assertNull reader
    }
}
