package org.skylight1.neny.model;

public enum Borough {
	MANHATTAN(1), THE_BRONX(2), BROOKLYN(3), QUEENS(4), STATEN_ISLAND(5);

	private int code;

	private Borough(final int aCode) {
		code = aCode;
	}

	/**
	 * Returns the borough associated with the code, or else null if the code is not that of a valid borough, e.g., 0.
	 * 
	 * @param aCode
	 * @return
	 */
	public static Borough findByCode(final int aCode) {
		for (final Borough borough : values()) {
			if (borough.code == aCode) {
				return borough;
			}
		}
		return null;
	}
}
