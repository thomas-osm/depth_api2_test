/**
Copyright (c) 2013-2015, Jens Kübler
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
 * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package net.sf.seesea.geometry.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import net.sf.seesea.geometry.IPoint;


public class MultiValueIterator implements Iterator<IPoint> {

	private final ListIterator<IPoint> iterator;
	
	private PointComparator comparator;

	public MultiValueIterator(ListIterator<IPoint> iterator) {
		this.iterator = iterator;
		comparator = new PointComparator();
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public IPoint next() {
		List<IPoint> points = new ArrayList<IPoint>(2);
		IPoint currentPoint = iterator.next();
		points.add(currentPoint);
		if(iterator.hasNext()) {
			IPoint nextPoint = iterator.next();
			while(true) {
				if(comparator.compare(currentPoint, nextPoint) == 0) {
					points.add(nextPoint);
					if(iterator.hasNext()) {
						nextPoint = iterator.next();
					} else {
						break;
					}
				} else {
					iterator.previous();
					break;
				}
			}
		}
		if(points.size() > 1) {
			Collections.sort(points, new DepthComparator());
			IPoint survivor = points.get(points.size() / 2);
			return survivor;
		}
		return currentPoint;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Remove is not implemented");
	}

}
