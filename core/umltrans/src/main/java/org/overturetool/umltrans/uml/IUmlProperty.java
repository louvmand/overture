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



public abstract class IUmlProperty extends IUmlNode {

// ***** VDMTOOLS START Name=vdmComp KEEP=NO
  static UTIL.VDMCompare vdmComp = new UTIL.VDMCompare();
// ***** VDMTOOLS END Name=vdmComp


// ***** VDMTOOLS START Name=vdm_init_IUmlProperty KEEP=NO
  private void vdm_init_IUmlProperty () throws CGException {}
// ***** VDMTOOLS END Name=vdm_init_IUmlProperty


// ***** VDMTOOLS START Name=IUmlProperty KEEP=NO
  public IUmlProperty () throws CGException {
    vdm_init_IUmlProperty();
  }
// ***** VDMTOOLS END Name=IUmlProperty


// ***** VDMTOOLS START Name=getName KEEP=NO
  abstract public String getName () throws CGException ;
// ***** VDMTOOLS END Name=getName


// ***** VDMTOOLS START Name=getVisibility KEEP=NO
  abstract public IUmlVisibilityKind getVisibility () throws CGException ;
// ***** VDMTOOLS END Name=getVisibility


// ***** VDMTOOLS START Name=getMultiplicity KEEP=NO
  abstract public IUmlMultiplicityElement getMultiplicity () throws CGException ;
// ***** VDMTOOLS END Name=getMultiplicity


// ***** VDMTOOLS START Name=hasMultiplicity KEEP=NO
  abstract public Boolean hasMultiplicity () throws CGException ;
// ***** VDMTOOLS END Name=hasMultiplicity


// ***** VDMTOOLS START Name=getType KEEP=NO
  abstract public IUmlType getType () throws CGException ;
// ***** VDMTOOLS END Name=getType


// ***** VDMTOOLS START Name=getIsReadOnly KEEP=NO
  abstract public Boolean getIsReadOnly () throws CGException ;
// ***** VDMTOOLS END Name=getIsReadOnly


// ***** VDMTOOLS START Name=hasIsReadOnly KEEP=NO
  abstract public Boolean hasIsReadOnly () throws CGException ;
// ***** VDMTOOLS END Name=hasIsReadOnly


// ***** VDMTOOLS START Name=getDefault KEEP=NO
  abstract public IUmlValueSpecification getDefault () throws CGException ;
// ***** VDMTOOLS END Name=getDefault


// ***** VDMTOOLS START Name=hasDefault KEEP=NO
  abstract public Boolean hasDefault () throws CGException ;
// ***** VDMTOOLS END Name=hasDefault


// ***** VDMTOOLS START Name=getIsComposite KEEP=NO
  abstract public Boolean getIsComposite () throws CGException ;
// ***** VDMTOOLS END Name=getIsComposite


// ***** VDMTOOLS START Name=getIsDerived KEEP=NO
  abstract public Boolean getIsDerived () throws CGException ;
// ***** VDMTOOLS END Name=getIsDerived


// ***** VDMTOOLS START Name=hasIsDerived KEEP=NO
  abstract public Boolean hasIsDerived () throws CGException ;
// ***** VDMTOOLS END Name=hasIsDerived


// ***** VDMTOOLS START Name=getIsStatic KEEP=NO
  abstract public Boolean getIsStatic () throws CGException ;
// ***** VDMTOOLS END Name=getIsStatic


// ***** VDMTOOLS START Name=hasIsStatic KEEP=NO
  abstract public Boolean hasIsStatic () throws CGException ;
// ***** VDMTOOLS END Name=hasIsStatic


// ***** VDMTOOLS START Name=getOwnerClass KEEP=NO
  abstract public String getOwnerClass () throws CGException ;
// ***** VDMTOOLS END Name=getOwnerClass


// ***** VDMTOOLS START Name=getQualifier KEEP=NO
  abstract public IUmlType getQualifier () throws CGException ;
// ***** VDMTOOLS END Name=getQualifier


// ***** VDMTOOLS START Name=hasQualifier KEEP=NO
  abstract public Boolean hasQualifier () throws CGException ;
// ***** VDMTOOLS END Name=hasQualifier

}
;
