/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 Ant-Contrib project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Ant-Contrib project (http://sourceforge.net/projects/ant-contrib)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The name Ant-Contrib must not be used to endorse or promote products 
 *    derived from this software without prior written permission. For
 *    written permission, please contact
 *    ant-contrib-developers@lists.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "Ant-Contrib"
 *    nor may "Ant-Contrib" appear in their names without prior written
 *    permission of the Ant-Contrib project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE ANT-CONTRIB PROJECT OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
//package ise.antelope.tasks;
package net.sf.antcontrib.property;

import org.apache.tools.ant.BuildException;

/**
 * Represents a number. 
 * <p>Developed for use with Antelope, migrated to ant-contrib Oct 2003.
 * @author Dale Anson, danson@germane-software.com
 * @version $Revision: 1.2 $
 */
public class Num {

   // the value of this number
   private String value = null;

   private String datatype = null;

   /**
    * Set the value for this number. This string must parse to the set
    * datatype, for example, setting value to "7.992" and datatype to INT
    * will cause a number format exception to be thrown. Supports two special
    * numbers, "E" and "PI".
    * @param value the value for this number   
    */
   public void setValue( String value ) {
      if (value.equals("E"))
         value = String.valueOf(java.lang.Math.E);
      else if (value.equals("PI"))
         value = String.valueOf(java.lang.Math.PI);
      this.value = value;
   }

   /**
    * @return the value for this number as a Number. Cast as appropriate to
    * Integer, Long, Float, or Double.
    */
   public Number getValue() {
      if (datatype == null)
         datatype = "double";
      if ( datatype.equals("int") )
         return new Integer( value );
      if ( datatype.equals("long" ))
         return new Long( value );
      if ( datatype.equals("float" ))
         return new Float( value );
      if ( datatype.equals("double" ))
         return new Double( value );
      throw new BuildException( "Invalid datatype." );
   }

   /**
    * Sets the datatype of this number. Allowed values are
    * "int", "long", "float", or "double".    
    */
   public void setDatatype( String p ) {
      datatype = p;
   }

   /**
    * @return the datatype as one of the defined types.   
    */
   public String getDatatype() {
      if (datatype == null)
         datatype = "double";
      return datatype;
   }
   
   public String toString() {
      return getValue().toString();  
   }
}

