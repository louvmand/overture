//
// THIS FILE IS AUTOMATICALLY GENERATED!!
//
// Generated at 2009-08-09 by the VDM++ to JAVA Code Generator
// (v8.2.1b - Wed 15-Jul-2009 14:09:22)
//
// Supported compilers: jdk 1.4/1.5/1.6
//

// ***** VDMTOOLS START Name=HeaderComment KEEP=NO
// ***** VDMTOOLS END Name=HeaderComment

// ***** VDMTOOLS START Name=package KEEP=NO
package org.overturetool.umltrans.uml;

// ***** VDMTOOLS END Name=package

// ***** VDMTOOLS START Name=imports KEEP=NO

import jp.co.csk.vdm.toolbox.VDM.*;
import java.util.*;
// ***** VDMTOOLS END Name=imports



public abstract class IUmlParameter extends IUmlNode {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp


// ***** VDMTOOLS START Name=vdm_init_IUmlParameter KEEP=NO
  private void vdm_init_IUmlParameter () throws CGException {}
// ***** VDMTOOLS END Name=vdm_init_IUmlParameter


// ***** VDMTOOLS START Name=IUmlParameter KEEP=NO
  public IUmlParameter () throws CGException {
    vdm_init_IUmlParameter();
  }
// ***** VDMTOOLS END Name=IUmlParameter


// ***** VDMTOOLS START Name=getName KEEP=NO
  abstract public String getName () throws CGException ;
// ***** VDMTOOLS END Name=getName


// ***** VDMTOOLS START Name=getType KEEP=NO
  abstract public IUmlType getType () throws CGException ;
// ***** VDMTOOLS END Name=getType


// ***** VDMTOOLS START Name=getMultiplicity KEEP=NO
  abstract public IUmlMultiplicityElement getMultiplicity () throws CGException ;
// ***** VDMTOOLS END Name=getMultiplicity


// ***** VDMTOOLS START Name=getDefault KEEP=NO
  abstract public String getDefault () throws CGException ;
// ***** VDMTOOLS END Name=getDefault


// ***** VDMTOOLS START Name=getDirection KEEP=NO
  abstract public IUmlParameterDirectionKind getDirection () throws CGException ;
// ***** VDMTOOLS END Name=getDirection

}
;
