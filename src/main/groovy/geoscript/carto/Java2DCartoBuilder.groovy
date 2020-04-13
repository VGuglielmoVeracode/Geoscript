package geoscript.carto

import geoscript.feature.Schema
import geoscript.geom.Geometry
import geoscript.layer.Layer
import geoscript.proj.Projection
import geoscript.render.Map
import geoscript.workspace.Memory

import javax.imageio.ImageIO
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.geom.GeneralPath
import java.text.AttributedString
import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * A CartoBuilder that draws Items using Java2D.
 * @author Jared Erickson
 */
class Java2DCartoBuilder implements CartoBuilder {

    protected final Graphics2D graphics

    protected final PageSize pageSize

    /**
     * Create a new Java2DCartoBuilder with a Graphics2D context and PageSize
     * @param graphics The Graphics2D context
     * @param pageSize The PageSize
     */
    Java2DCartoBuilder(Graphics2D graphics, PageSize pageSize) {
        this.graphics = graphics
        this.pageSize = pageSize
    }

    @Override
    CartoBuilder dateText(DateTextItem dateTextItem) {
        if (dateTextItem.date == null) dateTextItem.date = new Date()
        graphics.color = dateTextItem.color
        graphics.font = dateTextItem.font
        def formatter = new SimpleDateFormat(dateTextItem.format)
        drawString(formatter.format(dateTextItem.date), dateTextItem.getRectangle(), dateTextItem.horizontalAlign, dateTextItem.verticalAlign)
        this
    }

    @Override
    CartoBuilder image(ImageItem imageItem) {
        def image = ImageIO.read(imageItem.path)
        graphics.drawImage(image, imageItem.x, imageItem.y, imageItem.width, imageItem.height, null)
        this
    }

    @Override
    CartoBuilder line(LineItem lineItem) {
        graphics.color = lineItem.strokeColor
        graphics.stroke = new BasicStroke(lineItem.strokeWidth)
        graphics.drawLine(lineItem.x,lineItem.y,lineItem.x+lineItem.width,lineItem.y+lineItem.height)
        this
    }

    @Override
    CartoBuilder map(MapItem mapItem) {
        mapItem.map.width = mapItem.width
        mapItem.map.height = mapItem.height
        graphics.drawImage(mapItem.map.renderToImage(), mapItem.x, mapItem.y, null)
        this
    }

    @Override
    CartoBuilder overViewMap(OverviewMapItem overviewMapItem) {
        overviewMapItem.overviewMap.width = overviewMapItem.width
        overviewMapItem.overviewMap.height = overviewMapItem.height
        if (overviewMapItem.zoomIntoBounds) {
            overviewMapItem.overviewMap.bounds = overviewMapItem.linkedMap.bounds.scale(overviewMapItem.scaleFactor)
        }
        Projection overviewMapProjection = overviewMapItem.overviewMap.proj ?: new Projection("EPSG:4326")
        Projection linkedMapProjection = overviewMapItem.linkedMap.proj ?: new Projection("EPSG:4326")
        Layer areaLayer = new Memory().create(new Schema("area","geom:Polygon:srid=${overviewMapProjection.epsg}"))
        Geometry geometry = Projection.transform(overviewMapItem.linkedMap.bounds.geometry, linkedMapProjection, overviewMapProjection)
        areaLayer.add([geom: geometry])
        areaLayer.style = overviewMapItem.areaStyle
        overviewMapItem.overviewMap.addLayer(areaLayer)
        graphics.drawImage(overviewMapItem.overviewMap.renderToImage(), overviewMapItem.x, overviewMapItem.y, null)
        overviewMapItem.overviewMap.layers.remove(areaLayer)
        this
    }

    @Override
    CartoBuilder northArrow(NorthArrowItem northArrowItem) {
        if(northArrowItem.style == NorthArrowStyle.North) {
            drawNorthArrow(northArrowItem)
        } else if(northArrowItem.style == NorthArrowStyle.NorthEastSouthWest) {
            drawNESWArrow(northArrowItem)
        }
        this
    }

    private void drawNorthArrow(NorthArrowItem northArrowItem) {

        int x = northArrowItem.x
        int y = northArrowItem.y
        int width = northArrowItem.width
        int height = northArrowItem.height
        int strokeWidth = northArrowItem.strokeWidth
        Color fillColor1 = northArrowItem.fillColor1
        Color fillColor2 = northArrowItem.fillColor2
        Color strokeColor1 = northArrowItem.strokeColor1
        Color strokeColor2 = northArrowItem.strokeColor2

        if (northArrowItem.drawText) {
            graphics.color = northArrowItem.textColor
            graphics.font = northArrowItem.font
            FontMetrics fontMetrics = graphics.fontMetrics
            String text = "N"
            int textHeight = fontMetrics.height
            drawString(text, new Rectangle(0, height - textHeight, width, textHeight), HorizontalAlign.CENTER, VerticalAlign.BOTTOM)
            height = height - textHeight
        }

        def path1 = new GeneralPath()
        path1.moveTo((x + width/2) as double, y)
        path1.lineTo(x, y + height)
        path1.lineTo((x + width / 2) as double, (y + height * 3/4) as double)
        path1.closePath()

        def path2 = new GeneralPath()
        path2.moveTo((x + width/2) as double, y)
        path2.lineTo(x + width, y + height)
        path2.lineTo((x + width/2) as double, (y + height * 3/4) as double)
        path2.closePath()

        graphics.stroke = new BasicStroke(strokeWidth)

        graphics.color = fillColor1
        graphics.fill(path1)
        graphics.color = strokeColor1
        graphics.draw(path1)

        graphics.color = fillColor2
        graphics.fill(path2)
        graphics.color = strokeColor2
        graphics.draw(path2)
    }

    private void drawNESWArrow(NorthArrowItem northArrowItem) {

        int x = northArrowItem.x
        int y = northArrowItem.y
        int width = northArrowItem.width
        int height = northArrowItem.height

        if (northArrowItem.drawText) {
            graphics.color = northArrowItem.textColor
            graphics.font = northArrowItem.font
            FontMetrics fontMetrics = graphics.fontMetrics
            int textWidth = [fontMetrics.stringWidth("N"), fontMetrics.stringWidth("E"), fontMetrics.stringWidth("S"), fontMetrics.stringWidth("W")].max()
            int textHeight = fontMetrics.height
            drawString("N", new Rectangle((width / 2 - textWidth / 2) as int, 0, textWidth, textHeight), HorizontalAlign.CENTER, VerticalAlign.TOP)
            drawString("E", new Rectangle(width - textWidth, (height / 2 - textHeight / 2) as int, textWidth, textHeight), HorizontalAlign.RIGHT, VerticalAlign.MIDDLE)
            drawString("S", new Rectangle((width / 2 - textWidth / 2) as int, height - textHeight, textWidth, textHeight), HorizontalAlign.CENTER, VerticalAlign.TOP)
            drawString("W", new Rectangle(0, (height / 2 - textHeight / 2) as int, textWidth, textHeight), HorizontalAlign.LEFT, VerticalAlign.MIDDLE)
            x = x + textWidth
            y = y + textHeight
            width = width - (textWidth * 2)
            height = height - (textHeight * 2)
        }

        Point north = new Point((x + (width / 2) as int) - 2, y)
        Point east  = new Point(x + width, y + (height / 2) as int)
        Point south = new Point((x + (width / 2) as int) - 2, y + height)
        Point west  = new Point(x, y + (height / 2) as int)

        Point mid = new Point(x + (width / 2) as int, y + (height / 2) as int)

        Point northEast = new Point(x + (width * 2/3) as int, y + (height * 1/3) as int)
        Point southEast  = new Point(x + (width * 2/3) as int, y + (height * 2/3) as int)
        Point southWest  = new Point(x + (width * 1/3) as int, y + (height * 2/3) as int)
        Point northWest  = new Point(x + (width * 1/3) as int, y + (height * 1/3) as int)

        // Fill
        graphics.color = northArrowItem.fillColor1
        graphics.fill(createPolygon(north, northEast, mid))
        graphics.color = northArrowItem.fillColor2
        graphics.fill(createPolygon(north, northWest, mid))

        graphics.color = northArrowItem.fillColor1
        graphics.fill(createPolygon(east, southEast, mid))
        graphics.color = northArrowItem.fillColor2
        graphics.fill(createPolygon(east, northEast, mid))

        graphics.color = northArrowItem.fillColor1
        graphics.fill(createPolygon(south, southWest, mid))
        graphics.color = northArrowItem.fillColor2
        graphics.fill(createPolygon(south, southEast, mid))

        graphics.color = northArrowItem.fillColor1
        graphics.fill(createPolygon(west, northWest, mid))
        graphics.color = northArrowItem.fillColor2
        graphics.fill(createPolygon(west, southWest, mid))

        // Stroke
        graphics.color = northArrowItem.strokeColor1
        graphics.draw(createPolygon(north, northEast, mid))
        graphics.color = northArrowItem.strokeColor2
        graphics.draw(createPolygon(north, northWest, mid))

        graphics.color = northArrowItem.strokeColor1
        graphics.draw(createPolygon(east, southEast, mid))
        graphics.color = northArrowItem.strokeColor2
        graphics.draw(createPolygon(east, northEast, mid))

        graphics.color = northArrowItem.strokeColor1
        graphics.draw(createPolygon(south, southWest, mid))
        graphics.color = northArrowItem.strokeColor2
        graphics.draw(createPolygon(south, southEast, mid))

        graphics.color = northArrowItem.strokeColor1
        graphics.draw(createPolygon(west, northWest, mid))
        graphics.color = northArrowItem.strokeColor2
        graphics.draw(createPolygon(west, southWest, mid))
    }

    private GeneralPath createPolygon(Point point1, Point point2, Point point3) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3)
        path.moveTo(point1.x as int, point1.y as int)
        path.lineTo(point2.x as int, point2.y as int)
        path.lineTo(point3.x as int, point3.y as int)
        path.closePath()
        path
    }

    @Override
    CartoBuilder text(TextItem textItem) {
        graphics.color = textItem.color
        graphics.font = textItem.font
        drawString(textItem.text, textItem.getRectangle(), textItem.horizontalAlign, textItem.verticalAlign)
        this
    }

    @Override
    CartoBuilder paragraph(ParagraphItem paragraphItem) {

        int x = paragraphItem.x
        int y = paragraphItem.y
        int width = paragraphItem.width
        int height = paragraphItem.height
        String text = paragraphItem.text
        Font font = paragraphItem.font
        Color color = paragraphItem.color

        graphics.color = color
        graphics.font = font

        def attributedString = new AttributedString(text)
        attributedString.addAttribute(TextAttribute.FONT, font)
        def paragraph = attributedString.iterator
        int paragraphStart = paragraph.beginIndex
        int paragraphEnd = paragraph.endIndex
        def context = graphics.fontRenderContext
        def lineMeasurer = new LineBreakMeasurer(paragraph, context)
        float breakWidth = width as float
        float drawPosY = y as float
        lineMeasurer.position = paragraphStart

        while(lineMeasurer.position < paragraphEnd) {
            def layout = lineMeasurer.nextLayout(breakWidth)
            float drawPosX = layout.isLeftToRight() ? x : breakWidth - layout.advance
            drawPosY += layout.ascent
            layout.draw(graphics, drawPosX, drawPosY)
            drawPosY += layout.descent + layout.leading
        }
        this
    }

    @Override
    CartoBuilder rectangle(RectangleItem rectangleItem) {

        int x = rectangleItem.x
        int y = rectangleItem.y
        int width = rectangleItem.width
        int height = rectangleItem.height
        Color strokeColor = rectangleItem.strokeColor
        Color fillColor = rectangleItem.fillColor
        int strokeWidth = rectangleItem.strokeWidth

        if (fillColor) {
            graphics.color = fillColor
            graphics.fillRect(x, y, width, height)
        }
        graphics.color = strokeColor
        graphics.stroke = new BasicStroke(strokeWidth)
        graphics.drawRect(x,y,width,height)

        this
    }

    @Override
    CartoBuilder grid(GridItem gridItem) {
        graphics.color = gridItem.strokeColor
        graphics.stroke = new BasicStroke(gridItem.strokeWidth)
        int numberWide = pageSize.width / gridItem.size
        int numberHigh = pageSize.height / gridItem.size
        (0..numberWide).each { int c ->
            int x = gridItem.x + gridItem.size * c
            (0..numberHigh).each { int r ->
                int y = gridItem.y + gridItem.size * r
                graphics.drawRect(x,y, gridItem.size, gridItem.size)
            }
        }
        this
    }

    @Override
    CartoBuilder scaleText(ScaleTextItem scaleTextItem) {

        Font font = scaleTextItem.font
        Color color = scaleTextItem.color
        String format = scaleTextItem.format
        Map map = scaleTextItem.map

        graphics.color = color
        graphics.font = font
        def formatter = new DecimalFormat(format)
        drawString("${scaleTextItem.prefixText}1:${formatter.format(map.scaleDenominator)}", scaleTextItem.getRectangle(), scaleTextItem.horizontalAlign, scaleTextItem.verticalAlign)

        this
    }

    @Override
    CartoBuilder table(TableItem tableItem) {

        // Draw Headers
        (0..<tableItem.columns.size()).each { int c ->

            graphics.font = tableItem.columnRowStyle.font
            FontMetrics fontMetrics = graphics.fontMetrics
            int columnWidth = tableItem.width / tableItem.columns.size()
            int lastColumnWidth = columnWidth + (tableItem.width - (columnWidth * tableItem.columns.size() - 1)) - 1
            int rowHeight = fontMetrics.height + 4

            Rectangle rectangle = new Rectangle(
                tableItem.x + columnWidth * c,
                tableItem.y,
                c == tableItem.columns.size() - 1 ? lastColumnWidth : columnWidth,
                rowHeight
            )

            graphics.color = tableItem.columnRowStyle.backGroundColor
            graphics.fillRect(rectangle.x as int, rectangle.y as int, rectangle.width as int, rectangle.height as int)

            graphics.color = tableItem.columnRowStyle.strokeColor
            graphics.drawRect(rectangle.x as int, rectangle.y as int, rectangle.width as int, rectangle.height as int)

            graphics.color = tableItem.columnRowStyle.textColor
            drawString(" ${tableItem.columns[c]}", rectangle, HorizontalAlign.LEFT, VerticalAlign.MIDDLE)
        }
        // Draw rows
        (0..<tableItem.rows.size()).each { int r ->

            boolean isEven = r % 2
            graphics.font = isEven ? tableItem.evenRowStyle.font : tableItem.oddRowStyle.font
            FontMetrics fontMetrics = graphics.fontMetrics
            int columnWidth = tableItem.width / tableItem.columns.size()
            int lastColumnWidth = columnWidth + (tableItem.width - (columnWidth * tableItem.columns.size() - 1)) - 1
            int rowHeight = fontMetrics.height + 4

            int rowY = (tableItem.y + rowHeight * r) + rowHeight
            java.util.Map values = tableItem.rows[r]

            (0..<tableItem.columns.size()).each { int c ->

                Rectangle rectangle = new Rectangle(
                    tableItem.x + columnWidth * c,
                    rowY,
                    c == tableItem.columns.size() - 1 ? lastColumnWidth : columnWidth,
                    rowHeight
                )

                graphics.color = isEven ? tableItem.evenRowStyle.backGroundColor : tableItem.oddRowStyle.backGroundColor
                graphics.fillRect(rectangle.x as int, rectangle.y as int, rectangle.width as int, rectangle.height as int)

                graphics.color = isEven ? tableItem.evenRowStyle.strokeColor : tableItem.oddRowStyle.strokeColor
                graphics.drawRect(rectangle.x as int, rectangle.y as int, rectangle.width as int, rectangle.height as int)

                graphics.color = isEven ? tableItem.evenRowStyle.textColor : tableItem.oddRowStyle.textColor
                drawString(" ${values[tableItem.columns[c]]}", rectangle, HorizontalAlign.LEFT, VerticalAlign.MIDDLE)
            }
        }
        this
    }

    private void drawString(String text, Rectangle rectangle, HorizontalAlign horizontalAlign, VerticalAlign verticalAlign) {
      drawString(text, rectangle, horizontalAlign, verticalAlign, false)
    }

    private void drawString(String text, Rectangle rectangle, HorizontalAlign horizontalAlign, VerticalAlign verticalAlign, boolean debug) {
        FontMetrics fontMetrics = graphics.getFontMetrics(graphics.font)
        int x
        if (horizontalAlign == HorizontalAlign.LEFT) {
            x = rectangle.x
        } else if (horizontalAlign == HorizontalAlign.CENTER) {
            x = rectangle.x + (rectangle.width - fontMetrics.stringWidth(text)) / 2
        } else if (horizontalAlign == HorizontalAlign.RIGHT) {
            x = rectangle.x +  rectangle.width - fontMetrics.stringWidth(text)
        }
        int y
        if (verticalAlign == VerticalAlign.TOP) {
            y = rectangle.y + fontMetrics.height - fontMetrics.descent
        } else if (verticalAlign == VerticalAlign.MIDDLE) {
            y = rectangle.y + ((rectangle.height - fontMetrics.height) / 2) + fontMetrics.height - fontMetrics.descent
        } else if (verticalAlign == VerticalAlign.BOTTOM) {
            y = rectangle.y + rectangle.height - fontMetrics.descent
        }
        if (debug) {
            graphics.drawRect(rectangle.x as int, rectangle.y as int, rectangle.width as int, rectangle.height as int)
            graphics.drawRect(x, y - fontMetrics.height + fontMetrics.descent, fontMetrics.stringWidth(text), fontMetrics.height)
        }
        graphics.drawString(text, x, y)
    }

    @Override
    void build(OutputStream outputStream) {
    }
}