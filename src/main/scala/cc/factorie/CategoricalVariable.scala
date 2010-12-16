/* Copyright (C) 2008-2010 University of Massachusetts Amherst,
   Department of Computer Science.
   This file is part of "FACTORIE" (Factor graphs, Imperative, Extensible)
   http://factorie.cs.umass.edu, http://code.google.com/p/factorie/
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package cc.factorie

/** For use with variables whose values are mapped to densely-packed integers from 0 and higher, using a CategoricalDomain.
    It can apply to a single Int value (as in CategoricalVariable) or a collection of indices (as in BinaryVectorVariable).
    All instances of such a subclass share the same domain. 
    @author Andrew McCallum */
trait CategoricalVars[T] extends DiscreteVars with DomainType[CategoricalDomain[T]] /* with ValueType[CategoricalValues] */ {
  // TODO Consider removing all "VariableType" declarations, now that we don't need them for Domain
  type VariableType <: CategoricalVars[T]
  type CategoryType = T
}

/** A DiscreteVar whose integers 0...N are associated with an categorical objects of type A.
    Concrete implementations include CategoricalVariable and CategoricalObservation. 
    @author Andrew McCallum */
trait CategoricalVar[A] extends CategoricalVars[A] with DiscreteVar with ValueType[CategoricalValue[A]] {
  type VariableType <: CategoricalVar[A]
  @deprecated("Use entryValue instead.")
  def categoryValue: A = value.entry // domain.get(intValue)
  def entryValue: A = if (value ne null) value.entry else null.asInstanceOf[A]
  //final def entryValue: A = value.entry // TODO Include this too?  Nice, clear name, but two redundant methods could be confusing.
  override def toString = printName + "(" + (if (entryValue == null) "null" else if (entryValue == this) "this" else entryValue.toString) + "=" + intValue + ")" // TODO Consider dropping the "=23" at the end.
} 

/** A DiscreteVariable whose integers 0...N are associated with an object of type A. 
    @author Andrew McCallum */
abstract class CategoricalVariable[A] extends DiscreteVariable with CategoricalVar[A] with MutableValue {
  // What I want to do is "extends DiscreteVariable(Domain[A].index(initialValue))
  // but there is no way to obtain this.getClass obtain the Domain for the constructor above, so we initialize with dummy 0
  // and then set to proper value in this(initialValue:A) constructor below.
  type VariableType <: CategoricalVariable[A]
  def this(initialEntry:A) = { this(); _set(domain.getValue(initialEntry).asInstanceOf[Value]) }
  def set(newValue:A)(implicit d: DiffList): Unit = set(domain.index(newValue))
  //final def category_=(newValue:A)(implicit d:DiffList = null) = set(newValue)
}

/** For variables holding a single, constant indexed value which is of Scala type T. 
    @author Andrew McCallum */
abstract class CategoricalObservation[A](theEntry:A) extends DiscreteObservation with CategoricalVar[A] {
  type VariableType <: CategoricalObservation[A]
  _initializeValue(domain.getValue(theEntry).asInstanceOf[Value])
}

/** When mixed in to a CategoricalVariable or CategoricalObservation, the variable's Domain will count the number of calls to 'index'.  
    Then you can reduce the size of the Domain by calling 'trimBelowCount' or 'trimBelowSize', 
    which will recreate the new mapping from categories to densely-packed non-negative integers. 
    In typical usage you would (1) read in the data, (2) trim the domain, (3) re-read the data with the new mapping, creating variables. 
 */





// ItemizedObservation support

// TODO Replace this with Catalog?
// But then we couldn't use syntax like:  Domain[Person].size

/** An Observation put into an index, and whose value is the Observation variable itself.  
    For example, you can create 10 'Person extends ItemizedObservation[Person]' objects, 
    and upon creation each will be mapped to a unique integer 0..9.
    p1 = new Person; p1.index == 0; p1.categoryValue == p1. 
    @author Andrew McCallum */
trait ItemizedObservation[This <: ItemizedObservation[This]] extends CategoricalVar[This] with ConstantValue {
  this: This =>
  type VariableType = This
  // Put the variable in the CategoricalDomain and remember it.
  // We could save memory by looking it up in the Domain each time, but speed is more important
  //val intValue = domain.index(this) 
  val value = domain.getValue(this)
  override def categoryValue = this // TODO Ug.  Not a great method name here.
}

/** A variable who value is a pointer to an ItemizedObservation.  It is useful for entity-attributes whose value is another variable. 
    @author Andrew McCallum */
/*
class ItemizedObservationRef[V<:ItemizedObservation[V]](v:V) extends CategoricalVariable[V](v) {
  type VariableType = ItemizedObservationRef[V]
}
*/
