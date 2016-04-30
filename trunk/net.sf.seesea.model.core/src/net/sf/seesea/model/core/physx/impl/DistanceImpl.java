/**
 * Copyright (c) 2010-2012, Jens Kübler
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package net.sf.seesea.model.core.physx.impl;

import net.sf.seesea.model.core.physx.Distance;
import net.sf.seesea.model.core.physx.DistanceType;
import net.sf.seesea.model.core.physx.LengthUnit;
import net.sf.seesea.model.core.physx.PhysxPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Distance</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link net.sf.seesea.model.core.physx.impl.DistanceImpl#getValue <em>Value</em>}</li>
 *   <li>{@link net.sf.seesea.model.core.physx.impl.DistanceImpl#getUnit <em>Unit</em>}</li>
 *   <li>{@link net.sf.seesea.model.core.physx.impl.DistanceImpl#getDistanceType <em>Distance Type</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DistanceImpl extends MeasurementImpl implements Distance {
	/**
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final double VALUE_EDEFAULT = 0.0;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected double value = VALUE_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected static final LengthUnit UNIT_EDEFAULT = LengthUnit.UNDEFINED;

	/**
	 * The cached value of the '{@link #getUnit() <em>Unit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnit()
	 * @generated
	 * @ordered
	 */
	protected LengthUnit unit = UNIT_EDEFAULT;

	/**
	 * The default value of the '{@link #getDistanceType() <em>Distance Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDistanceType()
	 * @generated
	 * @ordered
	 */
	protected static final DistanceType DISTANCE_TYPE_EDEFAULT = DistanceType.UNKNOWN;

	/**
	 * The cached value of the '{@link #getDistanceType() <em>Distance Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDistanceType()
	 * @generated
	 * @ordered
	 */
	protected DistanceType distanceType = DISTANCE_TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DistanceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PhysxPackage.Literals.DISTANCE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public double getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(double newValue) {
		double oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysxPackage.DISTANCE__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LengthUnit getUnit() {
		return unit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setUnit(LengthUnit newUnit) {
		LengthUnit oldUnit = unit;
		unit = newUnit == null ? UNIT_EDEFAULT : newUnit;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysxPackage.DISTANCE__UNIT, oldUnit, unit));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DistanceType getDistanceType() {
		return distanceType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setDistanceType(DistanceType newDistanceType) {
		DistanceType oldDistanceType = distanceType;
		distanceType = newDistanceType == null ? DISTANCE_TYPE_EDEFAULT : newDistanceType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PhysxPackage.DISTANCE__DISTANCE_TYPE, oldDistanceType, distanceType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PhysxPackage.DISTANCE__VALUE:
				return getValue();
			case PhysxPackage.DISTANCE__UNIT:
				return getUnit();
			case PhysxPackage.DISTANCE__DISTANCE_TYPE:
				return getDistanceType();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PhysxPackage.DISTANCE__VALUE:
				setValue((Double)newValue);
				return;
			case PhysxPackage.DISTANCE__UNIT:
				setUnit((LengthUnit)newValue);
				return;
			case PhysxPackage.DISTANCE__DISTANCE_TYPE:
				setDistanceType((DistanceType)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PhysxPackage.DISTANCE__VALUE:
				setValue(VALUE_EDEFAULT);
				return;
			case PhysxPackage.DISTANCE__UNIT:
				setUnit(UNIT_EDEFAULT);
				return;
			case PhysxPackage.DISTANCE__DISTANCE_TYPE:
				setDistanceType(DISTANCE_TYPE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PhysxPackage.DISTANCE__VALUE:
				return value != VALUE_EDEFAULT;
			case PhysxPackage.DISTANCE__UNIT:
				return unit != UNIT_EDEFAULT;
			case PhysxPackage.DISTANCE__DISTANCE_TYPE:
				return distanceType != DISTANCE_TYPE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (value: "); //$NON-NLS-1$
		result.append(value);
		result.append(", unit: "); //$NON-NLS-1$
		result.append(unit);
		result.append(", distanceType: "); //$NON-NLS-1$
		result.append(distanceType);
		result.append(')');
		return result.toString();
	}

} //DistanceImpl
