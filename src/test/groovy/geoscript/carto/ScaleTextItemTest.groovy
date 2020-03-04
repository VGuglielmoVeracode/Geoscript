package geoscript.carto

import geoscript.layer.Shapefile
import geoscript.render.Map
import org.junit.Test

import java.awt.*
import java.text.SimpleDateFormat

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class ScaleTextItemTest {

    @Test
    void create() {

        Map map = new Map(layers: [
            new Shapefile(new File(getClass().getClassLoader().getResource("states.shp").toURI()))
        ])

        ScaleTextItem item = new ScaleTextItem(10,20,300,400)
            .font(new Font("Verdana", Font.BOLD, 14))
            .color(Color.BLUE)
            .verticalAlign(VerticalAlign.MIDDLE)
            .horizontalAlign(HorizontalAlign.CENTER)
            .prefixText("Scale: ")
            .format("#")
            .map(map)

        assertEquals(map, item.map)
        assertEquals(10, item.x)
        assertEquals(20, item.y)
        assertEquals(300, item.width)
        assertEquals(400, item.height)
        assertEquals(item.font.name, "Verdana")
        assertEquals(item.font.style, Font.BOLD)
        assertEquals(item.font.size, 14)
        assertEquals(item.color, Color.BLUE)
        assertEquals(VerticalAlign.MIDDLE, item.verticalAlign)
        assertEquals(HorizontalAlign.CENTER, item.horizontalAlign)
        assertEquals("Scale: ", item.prefixText)
        assertEquals("#", item.format)
        assertTrue(item.toString().startsWith("ScaleTextItem(x = 10, y = 20, width = 300, height = 400, " +
                "prefixText = Scale: , color = java.awt.Color[r=0,g=0,b=255], " +
                "font = java.awt.Font[family=Verdana,name=Verdana,style=bold,size=14], " +
                "horizontal-align = CENTER, vertical-align = MIDDLE, map = "))
        assertTrue(item.toString().endsWith(", format = #)"))
    }


}
