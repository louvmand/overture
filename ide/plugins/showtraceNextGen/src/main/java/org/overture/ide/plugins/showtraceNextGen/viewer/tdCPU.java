/*******************************************************************************
 * Copyright (c) 2009, 2011 Overture Team and others.
 *
 * Overture is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Overture is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Overture.  If not, see <http://www.gnu.org/licenses/>.
 * 	
 * The Overture Tool web-site: http://overturetool.org/
 *******************************************************************************/
// Decompiled by DJ v3.7.7.81 Copyright 2004 Atanas Neshkov  Date: 31-07-2009 16:17:13
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   tdCPU.java

package org.overture.ide.plugins.showtraceNextGen.viewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.overture.interpreter.messages.rtlog.nextgen.NextGenBus;
import org.overture.interpreter.messages.rtlog.nextgen.NextGenCpu;
import org.overture.interpreter.messages.rtlog.nextgen.NextGenRTLogger;

import jp.co.csk.vdm.toolbox.VDM.CGException;
import jp.co.csk.vdm.toolbox.VDM.UTIL;

// Referenced classes of package org.overturetool.tracefile.viewer:
//            tdResource, TraceData, tdThread, tdObject
@SuppressWarnings({"unchecked","rawtypes"})
public class tdCPU extends tdResource
{
	static jp.co.csk.vdm.toolbox.VDM.UTIL.VDMCompare vdmComp = new jp.co.csk.vdm.toolbox.VDM.UTIL.VDMCompare();
    private Long id;
	private NextGenRTLogger rtLogger;
    private String name;
    private Boolean expl;
    private HashMap<Long, tdObject> objects;
    private HashMap<Long, tdThread> threads;
    
    public tdCPU(int cpuID)
    {
    	rtLogger = NextGenRTLogger.getInstance();
    	Map<Integer, NextGenCpu> cpus = rtLogger.getCpuMap();
    	NextGenCpu cpu = cpus.get(cpuID);
    	objects = new HashMap<Long, tdObject>();
    	threads = new HashMap<Long, tdThread>();
    	
    	if(cpu != null)
    	{
    		id = new Long(cpu.id);
            name = cpu.name;
            expl = cpu.expl;
    	}
    	else
    	{
    		//TODO MAA: Exception?
    	}

    }

    public Long getId()
        throws CGException
    {
        return id;
    }
 
    public String getName()
        throws CGException
    {
        return name;
    }

    public Boolean isExplicit()
        throws CGException
    {
        return expl;
    }

    public HashSet connects()
        throws CGException
    {
    	HashSet res = new HashSet();
    	
    	Map<Integer, NextGenBus> buses = rtLogger.getBusMap();
    	
    	for(Integer key : buses.keySet())
    	{
    		NextGenBus currentBus = buses.get(key);
    		List<NextGenCpu> currentBusCpus = currentBus.cpus;
    		
    		for (NextGenCpu currentCpu : currentBusCpus) {
				
    			if(id.intValue() == currentCpu.id)
    				res.add(new Long(currentBus.id));
			}  		
    	}
    	
    	
    	
        return res; //TODO
    }

    public tdThread getThread(Long pthrid)
        throws CGException
    {

        //return (tdThread)threads.get(pthrid);
        return new tdThread(); //TODO
    }

    public void setCurrentThread(Long pthr)
        throws CGException
    {
        //TODO
    }

    public Boolean hasCurrentThread()
        throws CGException
    {
        return false; //TODO
    }

    public tdThread getCurrentThread()
        throws CGException
    {
        return new tdThread(); //TODO
    }

    public void addObject(tdObject pobj)
        throws CGException
    {
     	if(hasObject(pobj.getId()))
		{
    		//TODO MVQ: Handle if map already contains object
     		throw new UnsupportedOperationException("tdCPU.getObject(): Object already exists");
		}
    	else
    	{
    		objects.put(pobj.getId(), pobj);
    	}
    }

    public Boolean hasObject(Long pobjid)
        throws CGException
    {
        return objects.containsKey(pobjid);
    }

    public Boolean hasObjectAt(Long objectId, Long time)
    		throws CGException
    {
    	//FIXME: For now we ignore the object <-> time relation
    	return hasObject(objectId);
    }
    
    public tdObject getObject(Long pobjid)
        throws CGException
    {
     	if(!hasObject(pobjid))
		{
     		//TODO MVQ: Handle this situation
    		throw new UnsupportedOperationException("tdCPU.getObject(): Object not found");
		}

     	return objects.get(pobjid);
    }

    public HashSet<Long> getObjects()
        throws CGException
    {
    	HashSet<Long> result = new HashSet<Long>();
    	for(tdObject obj : objects.values())
    	{
    		result.add(obj.getId());
    	}
    	
        return result;
    }

    @Override
	public void reset()
        throws CGException
    {
       //TODO
    }

}