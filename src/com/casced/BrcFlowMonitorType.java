package com.casced;

public enum BrcFlowMonitorType {

	Normal, Monitor, Warning, Violation, Unknown;
	
	public static BrcFlowMonitorType fromConfig(String cfg)
	{
		switch(cfg.toLowerCase())
		{
			case "normal": return Normal;			
			case "monitor": return Monitor;			
			case "warning": return Warning;			
			case "violation": return Violation;
			case "unknown": return Unknown;
			default: return Unknown;
		}
	}

}
