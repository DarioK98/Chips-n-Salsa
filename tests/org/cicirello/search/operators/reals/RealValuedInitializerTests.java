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
 
package org.cicirello.search.operators.reals;

import org.junit.*;
import static org.junit.Assert.*;
import org.cicirello.search.representations.SingleReal;
import org.cicirello.search.representations.RealVector;
import org.cicirello.search.representations.RealValued;

/**
 * JUnit 4 test cases for the classes that implement Initializer for the
 * RealValued classes.
 */
public class RealValuedInitializerTests {
	
	// For tests involving randomness, number of test samples.
	private final int NUM_SAMPLES = 100;
	
	// precision for floating point equals comparisons
	private final double EPSILON = 1e-10;
	
	@Test
	public void testUnivariate() {
		SingleReal theClass = new SingleReal();
		double a = 3.0;
		double b = 11.0;
		RealValueInitializer f = new RealValueInitializer(a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("positive interval", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			SingleReal copy = g.copy(); 
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
			g.set(0, a - 1);
			assertEquals("verify unbounded set", a-1, g.get(0), EPSILON);
			g.set(0, b + 1);
			assertEquals("verify unbounded set", b+1, g.get(0), EPSILON);
		}
		a = -13.0;
		b = -2.0;
		f = new RealValueInitializer(a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("negative interval", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			SingleReal copy = g.copy(); 
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		a = -5.0;
		b = 5.0;
		f = new RealValueInitializer(a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("interval surrounding 0", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			SingleReal copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
	}
	
	@Test
	public void testBoundedUnivariate() {
		double a = 3.0;
		double b = 11.0;
		double min = 0;
		double max = 20;
		RealValueInitializer f = new RealValueInitializer(a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("bounds wider than interval", g.get(0) < b && g.get(0) >= a);
			g.set(0, min - 1);
			assertEquals("verify lower bound works on set", min, g.get(0), EPSILON);
			g.set(0, max + 1);
			assertEquals("verify upper bound works on set", max, g.get(0), EPSILON);
			g.set(0, 10);
			assertEquals("verify within bounds set", 10, g.get(0), EPSILON);
			SingleReal copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a;
		max = b;
		f = new RealValueInitializer(a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("bounds equal to interval", g.get(0) < b && g.get(0) >= a);
			SingleReal copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a + 1;
		max = b - 1;
		f = new RealValueInitializer(a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			SingleReal g = f.createCandidateSolution();
			assertTrue("bounds narrower than interval", g.get(0) <= max && g.get(0) >= min);
			SingleReal copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
	}
	
	@Test
	public void testMultivariate() {
		RealVector theClass = new RealVector(10);
		double a = 3;
		double b = 11;
		int n = 1;
		RealVectorInitializer f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertTrue("positive interval, one var", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		a = -13;
		b = -2;
		f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertTrue("negative interval, one var", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		a = -5;
		b = 5;
		f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertTrue("interval surrounding 0, one var", g.get(0) < b && g.get(0) >= a);
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		n = 10;
		a = 3;
		b = 11;
		f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("positive interval, ten vars", g.get(j) < b && g.get(j) >= a);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		a = -13;
		b = -2;
		f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("negative interval, ten vars", g.get(j) < b && g.get(j) >= a);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		a = -5;
		b = 5;
		f = new RealVectorInitializer(n, a, b);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("interval surrounding 0, ten vars", g.get(j) < b && g.get(j) >= a);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		double[] left = {  3, -13, -5, 4};
		double[] right = {11,  -2,  5, 4.1};
		n = 4;
		f = new RealVectorInitializer(left, right);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify runtime class is correct", theClass.getClass(), g.getClass());
			assertEquals("verify number of input variables", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("four vars different intervals", g.get(j) < right[j] && g.get(j) >= left[j]);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
			for (int j = 0; j < n; j++) {
				g.set(j, left[j] - 1);
				assertEquals("verify unbounded set", left[j]-1, g.get(j), EPSILON);
				g.set(j, right[j] + 1);
				assertEquals("verify unbounded set", right[j]+1, g.get(j), EPSILON);
			}
		}
	}
	
	@Test
	public void testBoundedMultivariate() {
		double a = 4;
		double b = 10;
		int n = 1;
		double min = 2;
		double max = 20;
		RealVectorInitializer f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			assertTrue("bounds wider than interval", g.get(0) < b && g.get(0) >= a);
			g.set(0, min - 1);
			assertEquals("verify lower bound works on set", min, g.get(0), EPSILON);
			g.set(0, max + 1);
			assertEquals("verify upper bound works on set", max, g.get(0), EPSILON);
			g.set(0, 10);
			assertEquals("verify within bounds set", 10, g.get(0), EPSILON);
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a;
		max = b;
		f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			assertTrue("bounds equal to interval", g.get(0) < b && g.get(0) >= a);
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a + 1;
		max = b - 1;
		f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			assertTrue("bounds narrower than interval", g.get(0) <= max && g.get(0) >= min);
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		
		n = 10;
		min = 2;
		max = 20;
		f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("bounds wider than interval", g.get(j) < b && g.get(j) >= a);
				g.set(j, min - 1);
				assertEquals("verify lower bound works on set", min, g.get(j), EPSILON);
				g.set(j, max + 1);
				assertEquals("verify upper bound works on set", max, g.get(j), EPSILON);
				g.set(j, 10);
				assertEquals("verify within bounds set", 10, g.get(j), EPSILON);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a;
		max = b;
		f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("bounds equal to interval", g.get(j) < b && g.get(j) >= a);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		min = a + 1;
		max = b - 1;
		f = new RealVectorInitializer(n, a, b, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("bounds narrower than interval", g.get(j) <= max && g.get(j) >= min);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		
		min = 6;
		max = 15;
		double[] left = {  7,   0,  0,  7, 8};
		double[] right = {11,  25, 11, 25, 8.1};
		n = 5;
		f = new RealVectorInitializer(left, right, min, max);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("verify in interval, different intervals", g.get(j) < right[j] && g.get(j) >= left[j]);
				assertTrue("verify in bounds, different intervals", g.get(j) <= max && g.get(j) >= min);
				g.set(j, min - 1);
				assertEquals("verify lower bound works on set", min, g.get(j), EPSILON);
				g.set(j, max + 1);
				assertEquals("verify upper bound works on set", max, g.get(j), EPSILON);
				g.set(j, 10);
				assertEquals("verify within bounds set", 10, g.get(j), EPSILON);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
		double[] mins = { 6, -15, -15, -15, 8};
		double[] maxs = {15,  -6,  15, 15,  8};
		left = new double[]  {8,  -18, 5, -30, 0};
		right = new double[] {12, -10, 18, 30, 20};
		f = new RealVectorInitializer(left, right, mins, maxs);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			RealVector g = f.createCandidateSolution();
			assertEquals("verify length", n, g.length());
			for (int j = 0; j < n; j++) {
				assertTrue("verify in interval, different bounds", g.get(j) < right[j] && g.get(j) >= left[j]);
				assertTrue("verify in bounds, different bounds", g.get(j) <= maxs[j] && g.get(j) >= mins[j]);
				g.set(j, mins[j] - 1);
				assertEquals("verify lower bound works on set", mins[j], g.get(j), EPSILON);
				g.set(j, maxs[j] + 1);
				assertEquals("verify upper bound works on set", maxs[j], g.get(j), EPSILON);
				g.set(j, (mins[j]+maxs[j])/2);
				assertEquals("verify within bounds set", (mins[j]+maxs[j])/2, g.get(j), EPSILON);
			}
			RealVector copy = g.copy();
			assertTrue("copy should be new object", copy != g);
			assertEquals("copy should be identical to original", g, copy);
			assertEquals("verify runtime class of copy", g.getClass(), copy.getClass());
		}
	}
	
	
}