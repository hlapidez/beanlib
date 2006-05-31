/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.beanlib.utils.range;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Joe D. Velopar
 */
public class SimpleRange implements RangeBoundable<Integer> 
{
    private final int lower;
    private final int upper;
    
    public SimpleRange(int lower, int upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public Integer getUpperBound() {
        return upper;
    }

    public Integer getLowerBound() {
        return lower;
    }

    public int compareTo(RangeBoundable<Integer> that) {
        int cmp = this.getLowerBound() - that.getLowerBound();
        return cmp == 0 ? this.getUpperBound() - that.getUpperBound() : cmp;
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (!(obj instanceof SimpleRange))
            return false;
        SimpleRange that = (SimpleRange)obj;
        
        return this.lower == that.lower 
            && this.upper == upper;
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
