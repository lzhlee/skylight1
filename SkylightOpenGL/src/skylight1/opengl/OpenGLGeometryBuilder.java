package skylight1.opengl;

import javax.microedition.khronos.opengles.GL10;

/**
 * Encapsulates the construction of OpenGLGeometry objects.
 */
public interface OpenGLGeometryBuilder<T, R> extends GeometryBuilder<T, R> {
	/**
	 * Used to mark the start of a new geometry. The end of the geometry is marked by a matching call to endGeometry. A
	 * given geometry must contain all triangles, triangle strips, triangle fans, lines, line strips, or points, not a
	 * mix there-of. All triangles, triangle strips, triangle fans, lines, line strips, or points added between the
	 * matching pair of startGeometry/endGeometry will belong to the single OpenGLGeometry object returned by
	 * endGeometry. Geometries may be nested, so two calls to startGeometry may be followed by two calls to endGeometry.
	 */
	void startGeometry(Texture aTexture);

	/**
	 * Returns true if all startGeometry calls have been matched with endGeometry calls.  Zero calls to both methods
	 * are considered to match;
	 */
	boolean isBuildingGeometry();

	/**
	 * @see OpenGLGeometryBuilder#startGeometry()
	 * @return The returned OpenGLGeometry must not be used until <i>after</i> this OpenGLGeometryBuilder has been
	 *         enabled.
	 */
	OpenGLGeometry endGeometry();

	/**
	 * Enables all of the features necessary to render any of the geometries created by this builder.
	 */
	void enable(GL10 aGL10);

	/**
	 * Resets the builder to as if it were just created. 
	 * This should only be used if it is known previously created geometries will never be used again.
	 * Using reset instead of creating a new instance reuses the large memory buffers.
	 */
	void reset();
}
