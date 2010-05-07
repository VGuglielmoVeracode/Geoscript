package geoscript.proj

import geoscript.geom.Geometry
import org.geotools.geometry.jts.GeometryCoordinateSequenceTransformer
import org.geotools.referencing.CRS
import org.opengis.referencing.crs.CoordinateReferenceSystem
import org.opengis.referencing.operation.MathTransform

/**
 * A Projection is a cartographic projection or coordinate reference system.
 * <p>You can create a Projection with an EPSG Code:</p>
 * <code>Projection p = new Projection("EPSG:4326")</code>
 * <p>Or with WKT:</p>
 * <code>Projection p = new Projection("""GEOGCS["GCS_WGS_1984",DATUM["D_WGS_1984",SPHEROID["WGS_1984",6378137,298.257223563]],PRIMEM["Greenwich",0],UNIT["Degree",0.017453292519943295]]""")</cod>
 * @author Jared Erickson
 */
class Projection {

    /**
     * The wrapped GeoTools CoordinateReferenceSystem
     */
    CoordinateReferenceSystem crs

    /**
     * By default GeoTools Referenceing assumes yx or lat/lon
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
     * Get the Identifier
     * @return The CRS Lookup Identifier
     */
    String getId() {
        // Sometimes CRS.lookupIdentifier returns null
        return CRS.lookupIdentifier(crs, true)
    }

    /**
     * Get the well known text
     * @return The well known texts
     */
    String getWkt() {
        return crs.toString()
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
        return Geometry.wrap(gtx.transform(geom.g))
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
        return projId != null ? projId : wkt
    }

    /**
     * Does this Projection equal the other?
     * @return Whether this Projection equal the other?
     */
    boolean equals(Object other) {
        if (!(other instanceof Projection))
        return false;
        return CRS.equalsIgnoreMetadata(crs, other.crs)
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
        return crs
    }

    /**
     * Reproject the Geometry from the source Projection to the destintation
     * Projection
     * @param geom The Geometry
     * @param src The Projection source/from
     * @param dest The Projection destination/to
     * @return A new Geometry
     */
    static Geometry transform(Geometry geom, Projection src, Projection dest) {
        return src.transform(geom, dest)
    }

    /**
     * Reproject the Geometry from the source Projection to the destintation
     * Projection
     * @param geom The Geometry
     * @param src The Projection String source/from
     * @param dest The Projection String destination/to
     * @return A new Geometry
     */
    static Geometry transform(Geometry geom, String src, String dest) {
        return new Projection(src).transform(geom, new Projection(dest))
    }


    /**
     * Get a List of all supported Projections.
     * This is currently reallllllly slow...
     * @return A List of all Projections
     */
    static List<Projection> projections() {
        List<Projection> projections = []
        CRS.getSupportedCodes("epsg").foreach{
            try {
                p = new Projection("EPSG:${it}")
                projectsion.add(p)
            }
            catch (Exception ex){
            }
        }
        return projections
    }
	
}
