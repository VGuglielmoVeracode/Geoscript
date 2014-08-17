package geoscript.proj

import geoscript.geom.Geometry
import geoscript.geom.Bounds
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer
import org.geotools.metadata.iso.citation.Citations
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import org.opengis.referencing.operation.MathTransform

/**
 * A Projection is a cartographic projection or coordinate reference system.
 * <p>You can create a Projection with an EPSG Code:</p>
 * <p><blockquote><pre>
 * Projection p = new Projection("EPSG:4326")
 * </pre></blockquote></p>
 * <p>Or with WKT:</p>
 * <p><blockquote><pre>
 * Projection p = new Projection("""GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137,298.257223563]],PRIMEM["Greenwich",0],UNIT["Degree",0.017453292519943295]]""")
 * </pre></blockquote></p>
 * @author Jared Erickson
 */
class Projection {

    /**
     * The wrapped GeoTools CoordinateReferenceSystem
     */
    CoordinateReferenceSystem crs

    /**
     * By default GeoTools Referencing assumes yx or lat/lon
     */
    static {
        if (!System.getProperty("org.geotools.referencing.forceXY")) {
            System.setProperty("org.geotools.referencing.forceXY", "true")
        }
    }

    /**
     * Create a new Projection wrapping a GeoTools
     * CoordinateReferenceSystem
     * @param crs The GeoTools CoordinateReferenceSystem
     */
    Projection(CoordinateReferenceSystem crs) {
        this.crs = crs
    }

    /**
     * Create a new Projection from an existing Projection
     * @param p An existing Projection
     */
    Projection(Projection p) {
        this(p.crs)
    }

    /**
     * Create a new Projection from a String
     * @param str A EPSG ID or WTK
     */
    Projection(String str) {
        this(parse(str))
    }

    /**
     * Get the Identifier.
     * This can be a very slow operation.
     * @return The CRS Lookup Identifier
     */
    String getId() {
        // Sometimes CRS.lookupIdentifier returns null
        CRS.lookupIdentifier(crs, true)
    }

    /**
     * Get the EPSG code
     * @return The EPSG code
     */
    int getEpsg() {
        CRS.lookupEpsgCode(crs, true)
    }

    /**
     * Get the well known text
     * @param citation The citation (can be epsg, the default, or esri)
     * @param indentation The number of spaces to indent (defaults to 2)
     * @return The well known texts
     */
    String getWkt(String citation = "epsg", int indentation = 2) {
        if (citation.equalsIgnoreCase("esri")) {
            ((org.geotools.referencing.wkt.Formattable)crs).toWKT(Citations.ESRI, indentation)
        } else {
            ((org.geotools.referencing.wkt.Formattable)crs).toWKT(Citations.EPSG, indentation)
        }
    }

    /**
     * Get the extent for this Projection
     * @return A Bounds
     */
    Bounds getBounds() {
        def extent = CRS.getEnvelope(crs)
        if (extent != null) {
            new Bounds(extent.getMinimum(0), extent.getMinimum(1), extent.getMaximum(0), extent.getMaximum(1), this)
        } else {
            null
        }
    }

    /**
     * Get the valid geographic area for this Projection
     * @return A Bounds
     */
    Bounds getGeoBounds() {
        def extent = CRS.getGeographicBoundingBox(crs)
        if (extent != null) {
            new Bounds(extent.westBoundLongitude, extent.southBoundLatitude, extent.eastBoundLongitude, extent.northBoundLatitude, 'epsg:4326')
        } else {
            null
        }
    }

    /**
     * Transform the Geometry to another Projection
     * @param geom The Geometry
     * @param dest The destination Projection
     * @return A new Geometry reprojected from this Projection to the destination Projection
     */
    Geometry transform(Geometry geom, Projection dest) {
        CoordinateReferenceSystem fromCrs = crs
        CoordinateReferenceSystem toCrs = dest.crs
        MathTransform tx = CRS.findMathTransform(fromCrs, toCrs)
        GeometryCoordinateSequenceTransformer gtx = new GeometryCoordinateSequenceTransformer()
        gtx.mathTransform = tx
        Geometry.wrap(gtx.transform(geom.g))
    }

    /**
     * Transform the Geometry to another Projection
     * @param geom The Geometry
     * @param dest The destination Projection string
     * @return A new Geometry reprojected from this Projection to the destination Projection
     */
    Geometry transform(Geometry geom, String prj) {
        transform(geom, new Projection(prj))
    }

    /**
     * The string representation
     * @return The string representation
     */
    String toString() {
        String projId = id
        projId != null ? projId : wkt
    }

    /**
     * Does this Projection equal the other?
     * @return Whether this Projection equal the other?
     */
    @Override
    boolean equals(Object other) {
        if (!(other instanceof Projection)) {
            false
        } else {
            CRS.equalsIgnoreMetadata(crs, other.crs)
        }

    }

    /**
     * Get the hashcode of this Projection
     * @return The hashcode
     */
    @Override
    int hashCode() {
        crs.hashCode()
    }

    /**
     * Try and parse the string as a CRS or WKT
     */
    private static CoordinateReferenceSystem parse(String str) {
        CoordinateReferenceSystem crs
        try {
            crs = CRS.decode(str)
        }
        catch(Exception ex1) {
            try {
                crs = CRS.parseWKT(str)
            }
            catch(Exception ex2) {
                throw new Exception("Unable to determine projection from ${str}!")
            }
        }
        crs
    }

    /**
     * Reproject the Geometry from the source Projection to the destination
     * Projection
     * @param geom The Geometry
     * @param src The Projection source/from
     * @param dest The Projection destination/to
     * @return A new Geometry
     */
    static Geometry transform(Geometry geom, Projection src, Projection dest) {
        src.transform(geom, dest)
    }

    /**
     * Reproject the Geometry from the source Projection to the destination
     * Projection
     * @param geom The Geometry
     * @param src The Projection String source/from
     * @param dest The Projection String destination/to
     * @return A new Geometry
     */
    static Geometry transform(Geometry geom, String src, String dest) {
        new Projection(src).transform(geom, new Projection(dest))
    }


    /**
     * Get a List of all supported Projections.
     * This is currently reallllllly slow...
     * @return A List of all Projections
     */
    static List<Projection> projections() {
        List<Projection> projections = []
        CRS.getSupportedCodes("epsg").each {
            try {
                projections.add(new Projection("EPSG:${it}"))
            }
            catch (Exception ex){
            }
        }
        projections
    }
}
