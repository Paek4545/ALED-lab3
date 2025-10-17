package es.upm.dit.aled.lab3.binary;

/**
 * Represents a suffix in the FASTA data by storing its starting index. This
 * class is designed to be sorted by an external Comparator that uses the actual
 * sequence data.
 *
 * @author mmiguel, rgarciacarmona
 */

/*  Suffix: Modela un sufijo, que no es más que un entero que indica en qué posición de
 content se encuentra dicho sufijo. */
public class Suffix {
	public final int suffixIndex;

	/**
	 * Creates a new Suffix.
	 * 
	 * @param index The starting position of the suffix in the data array.
	 */
	public Suffix(int index) {
		suffixIndex = index;
	}
}