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
 
package org.cicirello.search.sa;

import org.cicirello.search.problems.Problem;
import org.cicirello.search.problems.OptimizationProblem;
import org.cicirello.search.problems.IntegerCostOptimizationProblem;
import org.cicirello.search.operators.UndoableMutationOperator;
import org.cicirello.search.operators.Initializer;
import org.cicirello.util.Copyable;
import org.cicirello.search.ProgressTracker;
import org.cicirello.search.SingleSolutionMetaheuristic;
import org.cicirello.search.SolutionCostPair;
import org.cicirello.search.SimpleLocalMetaheuristic;

/**
 * <p>This class is an implementation of the metaheuristic known as simulated annealing.
 * Simulated annealing operates via a mechanism modeled after the process of heating a 
 * metal and allowing it to cool slowly. Heating enables the material to be shaped as 
 * desired, while cooling at a slow rate minimizes internal stress thus enabling 
 * greater stability in the final state.  Simulated annealing is a form of local search
 * inspired by this process.  It is controlled by a temperature parameter, T, that typically
 * begins high, and is then typically cooled during the search (i.e., T usually decreases 
 * during the search).  A component of the algorithm known as the annealing schedule controls
 * how the temperature T changes during the search.  Simulated annealing usually begins with
 * a random initial candidate solution to the problem.  Each iteration of simulated annealing
 * then involves generating a random neighbor of the current candidate solution, and deciding 
 * whether or not to accept it (if accepted, the algorithm moves to the neighbor).
 * The decision of whether or not to accept the neighbor is probabilistic.  If the neighbor's cost
 * is at least as good as the current cost, then the neighbor is definitely accepted.  If the
 * neighbor's cost is worse than (i.e., higher than) the current cost, 
 * then the neighbor is accepted with
 * probability, P(accept) = e<sup>(currentCost-neighborCost)/T</sup>.  This is known as the Boltzmann
 * distribution.  At high temperatures, there is a higher probability of accepting neighboring solutions
 * than at lower temperatures.  The probability of accepting neighbors is also higher for lower cost
 * neighbors than for higher cost neighbors.</p>
 *
 * <p>The factory methods of this class enable specifying an annealing schedule via a class
 * that implements the {@link AnnealingSchedule} interface, and the library provides all
 * of the common annealing schedules, such as exponential cooling, and linear cooling,
 * as well as a few less common, such as parameter-free versions of those schedules, as well as
 * an adaptive schedule known as the Modified Lam annealing schedule.  See the {@link AnnealingSchedule}
 * documentation for a list of the classes that implement this interface.  You may also implement
 * your own annealing schedule by implementing the {@link AnnealingSchedule} interface.</p>
 *
 * <p>You must also provide the factory methods of this class with a mutation operator for generating
 * random neighbors via a class that implements the {@link UndoableMutationOperator} interface, as
 * well as an instance of a class that implements the {@link Initializer} interface to provide
 * simulated annealing with a means of generating an initial random starting solution.  The library
 * provides several mutation operators for commonly optimized structures, as well as {@link Initializer}
 * objects for commonly optimized structures.  You are not limited to the implementations of
 * {@link UndoableMutationOperator} and {@link Initializer} provided in the library, and may
 * implement classes that implement these interfaces as necessary for your application.</p>
 *
 * <p>This simulated annealing implementation supports an optional post-processing via a hill climber.
 * To use this feature, you must use one of the factory methods that accepts a hill climber as a 
 * parameter.  This hill climber is then used to locally optimize the end of run solution 
 * generated by simulated annealing.  This
 * hill climber must implement the {@link SimpleLocalMetaheuristic} 
 * interface, such as the most commonly used
 * hill climbers (steepest descent and first descent) implemented by the 
 * {@link org.cicirello.search.hc.SteepestDescentHillClimber SteepestDescentHillClimber} 
 * and {@link org.cicirello.search.hc.FirstDescentHillClimber FirstDescentHillClimber} classes.</p>
 *
 * <p>Instances of SimulatedAnnealing are created through static factory methods named
 * {@link #createInstance}, rather than constructors.</p>
 *
 * @param <T> The type of object under optimization.
 *
 * @since 1.0
 *
 * @author <a href=https://www.cicirello.org/ target=_top>Vincent A. Cicirello</a>, 
 * <a href=https://www.cicirello.org/ target=_top>https://www.cicirello.org/</a>
 * @version 6.15.2020
 */
public class SimulatedAnnealing<T extends Copyable<T>> implements SingleSolutionMetaheuristic<T> {
	
	private final Initializer<T> initializer;
	private final UndoableMutationOperator<T> mutation;
	private final AnnealingSchedule anneal;
	private int elapsedEvals;
	private ProgressTracker<T> tracker;
	private final SingleRun<T> sr;
		
	/*
	 * internal constructor
	 */
	private SimulatedAnnealing(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker) {
		if (problem == null || mutation == null || anneal == null || initializer == null || tracker == null) {
			throw new NullPointerException();
		}
		this.initializer = initializer;
		this.mutation = mutation;
		this.anneal = anneal;
		this.tracker = tracker;
		// default on purpose: elapsedEvals = 0;
		sr = new IntCost(problem);
	}
	
	/*
	 * internal constructor
	 */
	private SimulatedAnnealing(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker) {
		if (problem == null || mutation == null || anneal == null || initializer == null || tracker == null) {
			throw new NullPointerException();
		}
		this.initializer = initializer;
		this.mutation = mutation;
		this.anneal = anneal;
		this.tracker = tracker;
		// default on purpose: elapsedEvals = 0;
		sr = new DoubleCost(problem);
	}
	
	/*
	 * private copy constructor in support of the split method.
	 * note: copies references to thread-safe components, and splits potentially non-threadsafe components 
	 */
	private SimulatedAnnealing(SimulatedAnnealing<T> other) {
		// this one must be shared.
		tracker = other.tracker;
	
		// split these: not threadsafe
		initializer = other.initializer.split();
		mutation = other.mutation.split();
		anneal = other.anneal.split();
		
		// the SingleRun object is tied to the SimulatedAnnealing instance.
		// need a new one
		Problem<T> p = other.sr.getProblem();
		@SuppressWarnings("unchecked")
		SingleRun<T> sr = p instanceof IntegerCostOptimizationProblem ? 
			new IntCost((IntegerCostOptimizationProblem<T>)p) : 
			new DoubleCost((OptimizationProblem<T>)p);
		this.sr = sr;
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, anneal, tracker);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, ProgressTracker<T> tracker) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, new ModifiedLam(), tracker);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, anneal, new ProgressTracker<T>());
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, new ModifiedLam(), new ProgressTracker<T>());
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, anneal, tracker);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, ProgressTracker<T> tracker) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, new ModifiedLam(), tracker);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, anneal, new ProgressTracker<T>());
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer) {
		return new SimulatedAnnealing<T>(problem, mutation, initializer, new ModifiedLam(), new ProgressTracker<T>());
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems
	 * that runs a hill climber as a post-processing step.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, anneal, tracker, hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}
	 * that runs a hill climber as a post-processing step.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, new ModifiedLam(), tracker, hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems
	 * that runs a hill climber as a post-processing step.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, anneal, new ProgressTracker<T>(), hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for real-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}
	 * that runs a hill climber as a post-processing step.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, new ModifiedLam(), new ProgressTracker<T>(), hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems
	 * that runs a hill climber as a post-processing step.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, anneal, tracker, hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}
	 * that runs a hill climber as a post-processing step.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param tracker A ProgressTracker object, which is used to keep track of the best
	 * solution found during the run, the time when it was found, and other related data.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, new ModifiedLam(), tracker, hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems
	 * that runs a hill climber as a post-processing step.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param anneal An annealing schedule.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, anneal, new ProgressTracker<T>(), hc);
	}
	
	/**
	 * Creates a SimulatedAnnealing search instance for integer-valued optimization problems, 
	 * with a default annealing schedule of {@link ModifiedLam}
	 * that runs a hill climber as a post-processing step.  
	 * A {@link ProgressTracker} is created for you.
	 * @param problem An instance of an optimization problem to solve.
	 * @param mutation A mutation operator supporting the undo operation.
	 * @param initializer The source of random initial states for simulated annealing.
	 * @param hc The Hill Climber that is used to locally optimize simulated annealing's end of
	 * run solution.  If hc.getProgressTracker() is not equal to tracker, then hc's ProgressTracker is
	 * reset to tracker.  That is, the ProgressTracker must be shared between the simulated annealer
	 * and the Hill Climber.
	 * @param <T> The type of object under optimization.
	 * @return an instance of SimulatedAnnealing configured as specified.
	 * @throws NullPointerException if any of the parameters are null.
	 * @throws IllegalArgumentException if problem is not equal to hc.getProblem()
	 */
	public static <T extends Copyable<T>> SimulatedAnnealing<T> createInstance(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, SimpleLocalMetaheuristic<T> hc) {
		return new SimulatedAnnealingHC<T>(problem, mutation, initializer, new ModifiedLam(), new ProgressTracker<T>(), hc);
	}
	
	
	/**
	 * Reaneals starting from the previous best found solution contained
	 * in the tracker object.  In reannealing, simulated annealing starts from
	 * a prior found solution rather than from a random one.  The
	 * annealing schedule is reinitialized (e.g., high starting temperature, etc) 
	 * as if it was a fresh run.  If no prior
	 * run had been performed, 
	 * then this method starts the run from a randomly generated
	 * solution.
	 * @param maxEvals The maximum number of simulated annealing evaluations (i.e., iterations)
	 * to execute.
	 * @return the current solution at the end of this run and its cost, which may or may not be the best
	 * of run solution, and which may or may not be the same as the solution contained
	 * in this simulated annealer's {@link ProgressTracker}, which contains the best of all runs.
	 * Returns null if the run did not execute, such as if the ProgressTracker already contains
	 * the theoretical best solution.
	 */
	@Override
	public final SolutionCostPair<T> reoptimize(int maxEvals) {
		ProgressTracker<T> tracker = getProgressTracker();
		if (tracker.didFindBest() || tracker.isStopped()) return null;
		T start = tracker.getSolution();
		if (start == null) start = initializer.createCandidateSolution();
		else start = start.copy();
		return optimizeSingleRun(maxEvals, start);
	}
	
	/**
	 * Executes a run of simulated annealing beginning at a randomly generated solution.
	 * If this method is called multiple times, each call begins at a new randomly generated
	 * starting solution, and reinitializes the annealing schedule (e.g., starting temperature, etc)
	 * as if it was a fresh run.  
	 * @param maxEvals The maximum number of simulated annealing evaluations (i.e., iterations)
	 * to execute during this run.
	 * @return The current solution at the end of this run and its cost, which may or may not be the best
	 * of run solution, and which may or may not be the same as the solution contained
	 * in this simulated annealer's {@link ProgressTracker}, which contains the best of all runs.
	 * Returns null if the run did not execute, such as if the ProgressTracker already contains
	 * the theoretical best solution.
	 */
	@Override
	public final SolutionCostPair<T> optimize(int maxEvals) {
		ProgressTracker<T> tracker = getProgressTracker();
		if (tracker.didFindBest() || tracker.isStopped()) return null;
		return optimizeSingleRun(maxEvals, initializer.createCandidateSolution());
	}
	
	/**
	 * Executes a run of simulated annealing beginning at a specified starting solution.
	 * If this method is called multiple times, each call begins by reinitializing 
	 * the annealing schedule (e.g., starting temperature, etc)
	 * as if it was a fresh run.  
	 * @param maxEvals The maximum number of simulated annealing evaluations (i.e., iterations)
	 * to execute during this run.
	 * @param start The desired starting solution.
	 * @return The current solution at the end of this run and its cost, which may or may not be the best
	 * of run solution, and which may or may not be the same as the solution contained
	 * in this simulated annealer's {@link ProgressTracker}, which contains the best of all runs.
	 * Returns null if the run did not execute, such as if the ProgressTracker already contains
	 * the theoretical best solution.
	 */
	@Override
	public final SolutionCostPair<T> optimize(int maxEvals, T start) {
		ProgressTracker<T> tracker = getProgressTracker();
		if (tracker.didFindBest() || tracker.isStopped()) return null;
		return optimizeSingleRun(maxEvals, start.copy());
	}
	
	@Override
	public final Problem<T> getProblem() {
		return sr.getProblem();
	}
	
	@Override
	public final ProgressTracker<T> getProgressTracker() {
		return tracker;
	}
	
	@Override
	public final void setProgressTracker(ProgressTracker<T> tracker) {
		if (tracker != null) this.tracker = tracker;
	}

	@Override
	public SimulatedAnnealing<T> split() {
		return new SimulatedAnnealing<T>(this);
	}
	
	/**
	 * <p>Gets the total number of simulated annealing evaluations (iterations)
	 * performed by this SimulatedAnnealing object.  This is the total number of
	 * such evaluations across all calls to the optimize and reoptimize methods.
	 * This may differ from the combined number of maxEvals passed as a parameter
	 * to those methods.  For example, those methods terminate 
	 * if they find the theoretical best solution, and also immediately return if
	 * a prior call found the theoretical best.  In such cases, the total run length may
	 * be less than the requested maxEvals.</p>
	 * <p>If the simulated annealer has been configured with hill climbing as a
	 * post-processing step, then the total run length includes both the simulated
	 * annealing iterations as well as the number of hill climbing neighbor evaluations.</p>
	 * @return the total number of simulated annealing evaluations
	 */
	@Override
	public long getTotalRunLength() {
		return elapsedEvals;
	}
	
	SolutionCostPair<T> optimizeSingleRun(int maxEvals, T current) {
		return sr.optimizeSingleRun(maxEvals, current);
	}
	
	private interface SingleRun<T extends Copyable<T>> {
		SolutionCostPair<T> optimizeSingleRun(int maxEvals, T current);
		Problem<T> getProblem();
	}
	
	private final class IntCost implements SingleRun<T> {
		
		private final IntegerCostOptimizationProblem<T> pOptInt;
		
		private IntCost(IntegerCostOptimizationProblem<T> problem) {
			pOptInt = problem;
		}
		
		@Override
		public IntegerCostOptimizationProblem<T> getProblem() {
			return pOptInt;
		}
		
		@Override
		public SolutionCostPair<T> optimizeSingleRun(int maxEvals, T current) {
			// compute cost of start
			int currentCost = pOptInt.cost(current);
			
			// initialize best cost, etc
			int bestCost = tracker.getCost();
			if (currentCost < bestCost) {
				bestCost = tracker.update(currentCost, current);
				if (bestCost == pOptInt.minCost()) {
					// found theoretical best so no point in proceeding
					tracker.setFoundBest();
					return new SolutionCostPair<T>(current, currentCost);
				}
			}
			
			// initialize the annealing schedule
			anneal.init(maxEvals);
			
			// main simulated annealing loop
			for (int i = 1; i <= maxEvals; i++) {
				if (tracker.isStopped()) {
					// some other thread signaled to stop
					elapsedEvals += (i-1);
					return new SolutionCostPair<T>(current, currentCost);
				}
				mutation.mutate(current);
				int neighborCost = pOptInt.cost(current);
				if (anneal.accept(neighborCost, currentCost)) {
					// accepting the neighbor
					currentCost = neighborCost;
					if (currentCost < bestCost) {
						bestCost = tracker.update(currentCost, current);
						if (bestCost == pOptInt.minCost()) {
							// found theoretical best so no point in proceeding
							tracker.setFoundBest();
							elapsedEvals += i;
							return new SolutionCostPair<T>(current, currentCost);
						}
					}
				} else {
					// reject the neighbor and revert back to previous state
					mutation.undo(current);
				}
			}
			elapsedEvals += maxEvals;
			return new SolutionCostPair<T>(current, currentCost);
		}
	}
	
	private final class DoubleCost implements SingleRun<T> {
		
		private final OptimizationProblem<T> pOpt;
		
		DoubleCost(OptimizationProblem<T> problem) {
			pOpt = problem;
		}
		
		@Override
		public final OptimizationProblem<T> getProblem() {
			return pOpt;
		}

		@Override
		public SolutionCostPair<T> optimizeSingleRun(int maxEvals, T current) {
			// compute cost of start
			double currentCost = pOpt.cost(current);
			
			// initialize best cost, etc
			double bestCost = tracker.getCostDouble();
			if (currentCost < bestCost) {
				bestCost = tracker.update(currentCost, current);
				if (bestCost == pOpt.minCost()) {
					// found theoretical best so no point in proceeding
					tracker.setFoundBest();
					return new SolutionCostPair<T>(current, currentCost);
				}
			}
			
			// initialize the annealing schedule
			anneal.init(maxEvals);
			
			// main simulated annealing loop
			for (int i = 1; i <= maxEvals; i++) {
				if (tracker.isStopped()) {
					// some other thread signaled to stop
					elapsedEvals += (i-1);
					return new SolutionCostPair<T>(current, currentCost);
				}
				mutation.mutate(current);
				double neighborCost = pOpt.cost(current);
				if (anneal.accept(neighborCost, currentCost)) {
					// accepting the neighbor
					currentCost = neighborCost;
					if (currentCost < bestCost) {
						bestCost = tracker.update(currentCost, current);
						if (bestCost == pOpt.minCost()) {
							// found theoretical best so no point in proceeding
							tracker.setFoundBest();
							elapsedEvals += i;
							return new SolutionCostPair<T>(current, currentCost);
						}
					}
				} else {
					// reject the neighbor and revert back to previous state
					mutation.undo(current);
				}
			}
			elapsedEvals += maxEvals;
			return new SolutionCostPair<T>(current, currentCost);
		}
	}
	
	/*
	 * Internal private subclass with hill climbing.
	 */
	private static class SimulatedAnnealingHC<T extends Copyable<T>> extends SimulatedAnnealing<T> {
		
		private final SimpleLocalMetaheuristic<T> hc;
	
		private SimulatedAnnealingHC(OptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
			super(problem, mutation, initializer, anneal, tracker);
			if (hc == null) throw new NullPointerException("hc must not be null");
			if (problem != hc.getProblem()) throw new IllegalArgumentException("hc must be configured with the same problem.");
			if (hc.getProgressTracker() != tracker) hc.setProgressTracker(tracker);
			this.hc = hc;
		}
		
		private SimulatedAnnealingHC(IntegerCostOptimizationProblem<T> problem, UndoableMutationOperator<T> mutation, Initializer<T> initializer, AnnealingSchedule anneal, ProgressTracker<T> tracker, SimpleLocalMetaheuristic<T> hc) {
			super(problem, mutation, initializer, anneal, tracker);
			if (hc == null) throw new NullPointerException("hc must not be null");
			if (problem != hc.getProblem()) throw new IllegalArgumentException("hc must be configured with the same problem.");
			if (hc.getProgressTracker() != tracker) hc.setProgressTracker(tracker);
			this.hc = hc;
		}
		
		/*
		 * private copy constructor in support of the split method.
		 * note: copies references to thread-safe components, 
		 * and splits potentially non-threadsafe components 
		 */
		private SimulatedAnnealingHC(SimulatedAnnealingHC<T> other) {
			super(other);
			hc = other.hc.split();
		}
		
		@Override
		public long getTotalRunLength() {
			return super.getTotalRunLength() + hc.getTotalRunLength();
		}
		
		@Override
		public SimulatedAnnealingHC<T> split() {
			return new SimulatedAnnealingHC<T>(this);
		}
		
		@Override
		SolutionCostPair<T> optimizeSingleRun(int maxEvals, T current) {
			SolutionCostPair<T> result = super.optimizeSingleRun(maxEvals, current);
			if (getProgressTracker().didFindBest() || getProgressTracker().isStopped()) {
				return result;
			} else {
				return hc.optimize(result.getSolution());
			}
		}
	}
}