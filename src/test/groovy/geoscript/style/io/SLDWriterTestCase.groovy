package geoscript.style.io

import geoscript.AssertUtil
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*
import geoscript.style.*

/**
 * The SLDWriter UnitTest
 * @author Jared Erickson
 */
class SLDWriterTestCase {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder()
    
    private String NEW_LINE = System.getProperty("line.separator")

    private String expectedSld = """<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" version="1.0.0">
  <sld:UserLayer>
    <sld:LayerFeatureConstraints>
      <sld:FeatureTypeConstraint/>
    </sld:LayerFeatureConstraints>
    <sld:UserStyle>
      <sld:Name>Default Styler</sld:Name>
      <sld:FeatureTypeStyle>
        <sld:Name>name</sld:Name>
        <sld:Rule>
          <sld:PolygonSymbolizer>
            <sld:Fill>
              <sld:CssParameter name="fill">#f5deb3</sld:CssParameter>
            </sld:Fill>
          </sld:PolygonSymbolizer>
          <sld:LineSymbolizer>
            <sld:Stroke>
              <sld:CssParameter name="stroke">#a52a2a</sld:CssParameter>
            </sld:Stroke>
          </sld:LineSymbolizer>
        </sld:Rule>
      </sld:FeatureTypeStyle>
    </sld:UserStyle>
  </sld:UserLayer>
</sld:StyledLayerDescriptor>"""

    @Test void writeToOutputStream() {
        Symbolizer sym = new Fill("wheat") + new Stroke("brown")
        SLDWriter writer = new SLDWriter();
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        writer.write(sym, out)
        String sld = out.toString().trim()
        assertNotNull sld
        assertTrue sld.length() > 0
        AssertUtil.assertStringsEqual expectedSld, sld, removeXmlNS: true, trim: true
    }

    @Test void writeToFile() {
        Symbolizer sym = new Fill("wheat") + new Stroke("brown")
        SLDWriter writer = new SLDWriter();
        File file = folder.newFile("simple.sld")
        writer.write(sym, file)
        String sld = file.text.trim()
        assertNotNull sld
        assertTrue sld.length() > 0
        AssertUtil.assertStringsEqual expectedSld, sld, removeXmlNS: true, trim: true
    }

    @Test void writeToString() {
        Symbolizer sym = new Fill("wheat") + new Stroke("brown")
        SLDWriter writer = new SLDWriter();
        String sld = writer.write(sym).trim()
        assertNotNull sld
        assertTrue sld.length() > 0
        AssertUtil.assertStringsEqual expectedSld, sld, removeXmlNS: true, trim: true
    }

    @Test void writeToStringWithOptions() {
        Symbolizer sym = new Fill("wheat") + new Stroke("brown")
        SLDWriter writer = new SLDWriter();
        String sld = writer.write(sym, exportDefaultValues: true, indentation: 4).trim()
        assertNotNull sld
        assertTrue sld.length() > 0
        String expected = """<?xml version="1.0" encoding="UTF-8"?>
<sld:StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:sld="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml" version="1.0.0">
    <sld:UserLayer>
        <sld:LayerFeatureConstraints>
            <sld:FeatureTypeConstraint/>
        </sld:LayerFeatureConstraints>
        <sld:UserStyle>
            <sld:Name>Default Styler</sld:Name>
            <sld:FeatureTypeStyle>
                <sld:Name>name</sld:Name>
                <sld:Rule>
                    <sld:PolygonSymbolizer>
                        <sld:Fill>
                            <sld:CssParameter name="fill">#f5deb3</sld:CssParameter>
                            <sld:CssParameter name="fill-opacity">1.0</sld:CssParameter>
                        </sld:Fill>
                    </sld:PolygonSymbolizer>
                    <sld:LineSymbolizer>
                        <sld:Stroke>
                            <sld:CssParameter name="stroke">#a52a2a</sld:CssParameter>
                            <sld:CssParameter name="stroke-linecap">butt</sld:CssParameter>
                            <sld:CssParameter name="stroke-linejoin">miter</sld:CssParameter>
                            <sld:CssParameter name="stroke-opacity">1.0</sld:CssParameter>
                            <sld:CssParameter name="stroke-width">1</sld:CssParameter>
                            <sld:CssParameter name="stroke-dashoffset">0.0</sld:CssParameter>
                        </sld:Stroke>
                    </sld:LineSymbolizer>
                </sld:Rule>
            </sld:FeatureTypeStyle>
        </sld:UserStyle>
    </sld:UserLayer>
</sld:StyledLayerDescriptor>"""
        AssertUtil.assertStringsEqual expected, sld, removeXmlNS: true, trim: true
    }
}
