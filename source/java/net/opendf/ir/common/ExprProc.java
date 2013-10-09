/* 
BEGINCOPYRIGHT X,UC
	
	Copyright (c) 2007, Xilinx Inc.
	Copyright (c) 2003, The Regents of the University of California
	All rights reserved.
	
	Redistribution and use in source and binary forms, 
	with or without modification, are permitted provided 
	that the following conditions are met:
	- Redistributions of source code must retain the above 
	  copyright notice, this list of conditions and the 
	  following disclaimer.
	- Redistributions in binary form must reproduce the 
	  above copyright notice, this list of conditions and 
	  the following disclaimer in the documentation and/or 
	  other materials provided with the distribution.
	- Neither the names of the copyright holders nor the names 
	  of contributors may be used to endorse or promote 
	  products derived from this software without specific 
	  prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND 
	CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
	INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
	CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
	HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
	CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
	OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	
ENDCOPYRIGHT
 */

package net.opendf.ir.common;

import java.util.Objects;

import net.opendf.ir.util.ImmutableList;
import net.opendf.ir.util.Lists;

public class ExprProc extends Expression {

	public <R, P> R accept(ExpressionVisitor<R, P> v, P p) {
		return v.visitExprProc(this, p);
	}

	public ExprProc(ImmutableList<ParDeclType> typeParams, ImmutableList<ParDeclValue> valueParams, Statement body) {
		this(null, typeParams, valueParams, body, ImmutableList.<Variable>empty(), false);
	}

	/**
	 * The parameter freeVariabls must have the correct set of variables, i.e. this constructor sets isFreeVariablesComputed to true.
	 * @param typeParams
	 * @param valueParams
	 * @param body
	 * @param freeVariables
	 */
	public ExprProc(ImmutableList<ParDeclType> typeParams, ImmutableList<ParDeclValue> valueParams, Statement body, ImmutableList<Variable> freeVariables) {
		this(null, typeParams, valueParams, body, freeVariables, true);
	}
	
	private ExprProc(ExprProc original, ImmutableList<ParDeclType> typeParams, ImmutableList<ParDeclValue> valueParams, Statement body, 
			         ImmutableList<Variable> freeVariables, boolean isFreeVariablesComputed) {
		super(original);
		assert typeParams != null && valueParams != null && body!= null && freeVariables != null;
		this.typeParameters = ImmutableList.copyOf(typeParams);
		this.valueParameters = ImmutableList.copyOf(valueParams);
		this.body = body;
		this.freeVariables = freeVariables;
		this.isFreeVariablesComputed = isFreeVariablesComputed;
	}
	
	public ExprProc copy(ImmutableList<ParDeclType> typeParams, ImmutableList<ParDeclValue> valueParams, Statement body, 
			             ImmutableList<Variable> freeVariables, boolean isFreeVariablesComputed) {
		if (Lists.equals(typeParameters, typeParams) && Lists.equals(valueParameters, valueParams) && Objects.equals(this.body, body)
				 && this.isFreeVariablesComputed==isFreeVariablesComputed && Lists.equals(this.freeVariables, freeVariables)) {
			return this;
		}
		return new ExprProc(this, typeParams, valueParams, body, freeVariables, isFreeVariablesComputed);
	}


	public ImmutableList<ParDeclType> getTypeParameters() {
		return typeParameters;
	}

	public ImmutableList<ParDeclValue> getValueParameters() {
		return valueParameters;
	}

	/**
	 * Before calling getFreeVariables() the free variables must be computed. 
	 * @return the free variables of the procedure
	 */
	public ImmutableList<Variable> getFreeVariables(){
		assert isFreeVariablesComputed;
		return freeVariables;
	}

	public boolean isFreeVariablesComputed(){
		return isFreeVariablesComputed;
	}

	public Statement getBody() {
		return body;
	}

	private ImmutableList<ParDeclType> typeParameters;
	private ImmutableList<ParDeclValue> valueParameters;
	private ImmutableList<Variable> freeVariables;
	private boolean isFreeVariablesComputed;
	private Statement body;
}
