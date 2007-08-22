--------------------------------------------------------------------------------
               Location API for J2ME (JSR 179 API) Demonstration
--------------------------------------------------------------------------------

1. Introduction

	CityGuide uses JSR179 Location API. It demonstrates basic usage of the LocationProvider,
	LocationListener, ProximityListener, Landmarks and LandmarkStore.


2. Project Structure

	The structure of the project is following:
	./waypoints.txt         text file for waypoints definition
	./citywalk.xml		simulation of the visitor path


3. Usage

	- Run the CityGuideMIDlet.
	- Midlet loads waypoints.txt and creates landmark store WTK_HOME/appdb/location/waypoints.lms.
	  You can browse it by Landmark Store Manager.
	- Open External Event Generator (Emulator window -> menu MIDlet -> External Events)
	- Browse for script citywalk.xml and run it
	- In the map browser you have 2 choices from the menu. 
	- Settings let you select which categories of waypoints you are interested in
	- Details shows you detail of the waypoints that are in your proximity radius


4. Required APIs
    
    JSR 139 - Connected Limited Device Configuration (CLDC) 1.1
    JSR 118 - Mobile Information Device Profile (MIDP) 2.0
    JSR 179 - Location API for J2ME
