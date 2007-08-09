/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package com.xruby.compiler.codedom;

import com.xruby.compiler.parser.ISourcePosition;


/** Represents an integer literal.
 *
 * @author  jpetersen
 */
public class FixnumNode extends Node implements ILiteralNode {
    static final long serialVersionUID = 2236565825959274729L;

    private int value;


    public FixnumNode(ISourcePosition position, int value) { //don't know why jruby use long here
        super(position, NodeTypes.FIXNUMNODE);
        this.value = value;
    }

    public void accept(CodeVisitor visitor) {
       visitor.visitFixnumExpression(value);
    }

    /**
     * Gets the value.
     * @return Returns a long
     */
    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = (int) value;
    }

}