package es.upm.dit.aled.lab3.binary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.upm.dit.aled.lab3.FASTAReader;

/**
 * Reads a FASTA file containing genetic information and allows for the search
 * of specific patterns within these data. The information is stored as an array
 * of bytes that contain nucleotides in the FASTA format. Since this array is
 * usually created before knowing how many characters in the origin FASTA file
 * are valid, an int indicating how many bytes of the array are valid is also
 * stored. All valid characters will be at the beginning of the array.
 * 
 * This extension of the FASTAReader uses a sorted dictionary of suffixes to
 * allow for the implementation of binary search.
 * 
 * @author mmiguel, rgarciacarmona
 */

/*  FASTAReaderSuffixes: Extiende FASTAReader para soportar la búsqueda binaria.
 */
public class FASTAReaderSuffixes extends FASTAReader {
	protected Suffix[] suffixes;


	/**
	 * Creates a new FASTAReader from a FASTA file.
	 * 
	 * At the end of the constructor, the data is sorted through an array of
	 * suffixes.
	 * 
	 * @param fileName The name of the FASTA file.
	 */
	public FASTAReaderSuffixes(String fileName) {
		// Calls the parent constructor
		super(fileName);
		this.suffixes = new Suffix[validBytes];
		for (int i = 0; i < validBytes; i++)
			suffixes[i] = new Suffix(i);
		// Sorts the data
		sort();
	}

	/*
	 * Helper method that creates a array of integers that contains the positions of
	 * all suffixes, sorted alphabetically by the suffix.
	 */
	private void sort() {
		// Instantiate the external SuffixComparator, passing 'this' (the reader)
		// so it can access the content and validBytes fields.
		SuffixComparator suffixComparator = new SuffixComparator(this);
		// Use the external Comparator for sorting.
		Arrays.sort(this.suffixes, suffixComparator);
	}

	/**
	 * Prints a list of all the suffixes and their position in the data array.
	 */
	public void printSuffixes() {
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("Index | Sequence");
		System.out.println("-------------------------------------------------------------------------");
		for (int i = 0; i < suffixes.length; i++) {
			int index = suffixes[i].suffixIndex;
			String ith = "\"" + new String(content, index, Math.min(50, validBytes - index)) + "\"";
			System.out.printf("  %3d | %s\n", index, ith);
		}
		System.out.println("-------------------------------------------------------------------------");
	}

	/**
	 * Implements a binary search to look for the provided pattern in the data
	 * array. Returns a List of Integers that point to the initial positions of all
	 * the occurrences of the pattern in the data.
	 * 
	 * @param pattern The pattern to be found.
	 * @return All the positions of the first character of every occurrence of the
	 *         pattern in the data.
	 */
	@Override
	/*  El método search() debe aprovecharse de la lista ordenada de sufijos para poder ejecutar
 una búsqueda binaria
 */
	public List<Integer> search(byte[] pattern) {
	    // Creamos la lista de posiciones a devolver (inicialmente vacía)
	    List<Integer> positions = new ArrayList<>();

	    // Inicializamos lo y hi
	    int lo = 0; // Índice más bajo a considerar
	    int hi = suffixes.length - 1; // Índice más alto a considerar

	    // Para determinar si se ha encontrado el patrón
	    boolean found = false;
	    // Contador usado para recorrer el patrón y compararlo carácter por carácter con el sufijo actual
	    int index = 0;
	    
	    // Búsqueda binaria
	    do {
	    // Calculamos el índice medio
	        int m = (int) Math.floor(lo + (hi - lo) / 2);
	        int posSuffix = suffixes[m].suffixIndex; // posición del sufijo
	        // Reinicia el índice del patrón antes de empezar a comparar con el sufijo
	        index = 0;

	        // Comparar carácter por carácter mientras no se salga del rango
	        while (index < pattern.length && //Comparamos todo el patrón
		               posSuffix + index < content.length && //Comparamos todo el contenido del genoma
	               pattern[index] == content[posSuffix + index]) { //Los caracteres no coinciden
	        	// Si hay coincidencia, incrementamos en 1 el índice
	            index++;
	        }

	        // Coincidencia completa
	        // Si el índice llega al final del patrón, añdimos la posición del sufijo y detenemos la búsqueda binaria
	        if (index == pattern.length) {
	            positions.add(posSuffix);
	            found = true;
	            
	            // Buscamos coincidencias hacia arriba (índices menores)
	            int i = m - 1;
	            while (i >= 0) {
	                int p = suffixes[i].suffixIndex;
	                int j = 0;
	                while (j < pattern.length &&
	                       p + j < content.length &&
	                       pattern[j] == content[p + j]) {
	                    j++;
	                }
	                if (j == pattern.length) {
	                    positions.add(0,p);
	                    i--;
	                } else {
	                    break; // Se acabaron las coincidencias hacia arriba, nos salimos del bucle while
	                }
	            }

	            // Buscamos las coincidencias hacia abajo (índices mayores)
	            int i2 = m + 1;
	            while (i2 < suffixes.length) {
	                int p2 = suffixes[i2].suffixIndex;
	                int j2 = 0;
	                while (j2 < pattern.length &&
	                       p2 + j2 < content.length &&
	                       pattern[j2] == content[p2 + j2]) {
	                    j2++;
	                }
	                if (j2 == pattern.length) {
	                    positions.add(p2);
	                    i2++;
	                } else {
	                    break; // Se acabaron las coincidencias hacia abajo
	                }
	            }
	        }
	        // Si todavía hay caracteres para comparar, decidir hacia dónde moverse
	        else if (index < pattern.length && posSuffix + index < content.length) {
	        	// Patrón menor
	            if ((pattern[index]) < (content[posSuffix + index])) {
	            // Reducimos el límite superior --> descartamos la mitad superior
	                hi = m - 1;
	            } else {
	            // Descartamos la mitad inferior
	                lo = m + 1;
	            }
	        }

	    } while (hi - lo > 1 && !found);

	    return positions;
	}


	public static void main(String[] args) {
		long t1 = System.nanoTime();
		FASTAReaderSuffixes reader = new FASTAReaderSuffixes(args[0]);
		if (args.length == 1)
			return;
		byte[] patron = args[1].getBytes();
		System.out.println("Tiempo de apertura de fichero: " + (System.nanoTime() - t1));
		long t2 = System.nanoTime();
		System.out.println("Tiempo de ordenación: " + (System.nanoTime() - t2));
		reader.printSuffixes();
		long t3 = System.nanoTime();
		List<Integer> posiciones = reader.search(patron);
		System.out.println("Tiempo de búsqueda: " + (System.nanoTime() - t3));
		if (posiciones.size() > 0) {
			for (Integer pos : posiciones)
				System.out.println("Encontrado " + args[1] + " en " + pos);
		} else
			System.out.println("No he encontrado " + args[1] + " en ningún sitio.");
		System.out.println("Tiempo total: " + (System.nanoTime() - t1));
	}
}
