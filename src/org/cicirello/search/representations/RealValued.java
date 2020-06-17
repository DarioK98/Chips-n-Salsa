/*
 * Chips-n-Salsa: A library of parallel self-adaptive local search algorithms.
 * Copyright (C) 2002-2020  Vincent A. Cicirello
 *
 * This file is part of Chips-n-Salsa (https://chips-n-salsa.cicirello.org/).
 * 
 * Chips-n-Salsa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Chips-n-Salsa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
 
package org.cicirello.search.representations;


/**
 * An interface to define the parameters to a function,
 * where the function parameters are represented as type double
 * (i.e., as double precision floating point numbers).
 * Classes implementing this interface may be applicable for
 * real-valued function optimization problems.
 *
 * @since 1.0
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, 
 * <a href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 * @version 6.5.2020
 */
public interface RealValued {
	
	/**
	 * Gets the number of parameters.
	 * @return The number of parameters for this function.
	 */
	int length();
	
	/**
	 * Accesses the current value of a specified parameter.
	 * @param i The parameter to get.
	 * @return The current value of the i-th parameter.
	 * @throws ArrayIndexOutOfBoundsException if i &lt; 0 or i &ge; length().
	 */
	double get(int i);
	
	/**
	 * Accesses the current values of all of the parameters.
	 * @param values An array to hold the result.  If values is null or
	 * if values.length is not equal to this.length(), then a new array 
	 * is constructed for the result.
	 * @return An array containing the current values of all of the parameters.
	 */
	double[] toArray(double[] values);
	
	/**
	 * Sets a function parameter to a specified value.
	 * @param i The parameter to set.
	 * @param value The new value for the i-th parameter.
	 * @throws ArrayIndexOutOfBoundsException if i &lt; 0 or i &ge; length().
	 */
	void set(int i, double value);
}