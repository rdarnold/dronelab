package dronelab.utils;

import java.util.ArrayList;
import java.util.List;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import dronelab.DroneLab;
import dronelab._3d.*;
import dronelab.collidable.*;
import dronelab.gui.*;
import dronelab.utils.*;

import javafx.concurrent.Task;
import javafx.application.Platform;

import java.lang.reflect.Method;

/**
 * Handles a server-side channel.
 */
public class NettyTCPServerHandler extends SimpleChannelInboundHandler<String> {

	// List of connected client channels.
	static final List<Channel> channels = new ArrayList<Channel>();


	/*
	 * Whenever client connects to server through channel, add his channel to the
	 * list of channels.
	 */
	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		System.out.println("Client joined - " + ctx);
		channels.add(ctx.channel());
	}



	// Contains the if statements to take actions on the GUI
	@Override
	public void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		String response = null;

		System.out.println("Server received - " + msg);
		String[] parts = msg.split("\\.");

		//Start button 
		if (msg.equals("Sim.Action.Start")){
			System.out.println("Channel Received Message - Start Button pushed ");
			Platform.runLater(
            () -> {
				DroneLab.sim.start();
            	}
       		);
			response = "Sim.Action.Start|Executed";
		}
		else if (msg.equals("Sim.Action.PerformAllRuns")){
			System.out.println("Channel Received Message - PerformAllRuns Button pushed ");
			Platform.runLater(
			() -> {
				DroneLab.sim.performAllRuns();
				}
			);
			response = "Sim.Action.PerformAllRuns|Executed";
		} else if (msg.equals("Sim.Action.Stop")){
			System.out.println("Channel Received Message - Stop Button pushed ");
			Platform.runLater(
            () -> {
				DroneLab.sim.stop();
            	}
       		);
			response = "Sim.Action.Stop|Executed";
		} else if (msg.equals("Sim.Action.Pause")){
			System.out.println("Channel Received Message - Pause Button pushed ");
			Platform.runLater(
            () -> {
				DroneLab.sim.handlePauseButton();
            	}
       		);
			response = "Sim.Action.Pause|Executed|";
		}
		else if (msg.equals("Sim.Get.CurrentTime")){
			System.out.println("Channel Received Message - Get Current Time ");
			response = "Sim.Get.CurrentTime|Executed|"+DroneLab.scenario.simTime.toString();
		}
		else if (msg.equals("Drone.Get.ActiveBehaviors")){
			System.out.println("Channel Received Message - Drone.Get.ActiveBehaviors");
			response = "Drone.Get.ActiveBehaviors|Executed|";
			for (Drone d : DroneLab.scenario.drones) {
				response += d.getId()+":"+d.lastActiveModule.getName()+",";
				
			}
			response = response.substring(0, response.length() - 1); 	
		}
		else if (msg.equals("Drone.Get.AllBehaviors")){
			System.out.println("Channel Received Message - Drone.Get.Drones");
			response = "Drone.Get.AllBehaviors|Executed|";
			for (Drone d : DroneLab.scenario.drones) {
				response += d.getId()+":"+d.printBehaviorsDelim()+",";
				
			}
			response = response.substring(0, response.length() - 1); 	
		}
		else if (msg.contains("Drone.Action.RemoveBehavior.")){
			String strTargetDroneId = parts[3];
			String strTargetDroneRole = parts[4];
			int targetDroneId = Integer.parseInt(strTargetDroneId);
			System.out.println("Channel Received Message - Drone.Action.RemoveBehavior."+targetDroneId);
			Platform.runLater(
					() -> {
						DroneLab.scenario.processRemoveBehaviorForOneDrone(strTargetDroneRole,targetDroneId);
						System.out.println("Executed Removal of Behavior "+strTargetDroneRole+" for Drone "+strTargetDroneRole);
						}
					);

			response = "Drone.Action.RemoveBehavior."+strTargetDroneId+"."+strTargetDroneRole+"|Executed|";
		}
		else if (msg.contains("Drone.Action.AddBehavior.")){
			String strTargetDroneId = parts[3];
			String strTargetDroneRole = parts[4];
			int targetDroneId = Integer.parseInt(strTargetDroneId);
			System.out.println("Channel Received Message - Drone.Action.AddBehavior."+targetDroneId);
			Platform.runLater(
					() -> {
						DroneLab.scenario.processAddBehaviorForOneDrone(strTargetDroneRole,targetDroneId);
						System.out.println("Executed Addition of Behavior "+strTargetDroneRole+" for Drone "+strTargetDroneRole);
						}
					);

			response = "Drone.Action.AddBehavior."+strTargetDroneId+"."+strTargetDroneRole+"|Executed|";
		}
		else if (msg.contains("Drone.Get.AllRoles")){
			System.out.println("Channel Received Message - Drone.Get.AllRoles");
			response = "Drone.Get.AllRoles|Executed|";
			for (Drone d : DroneLab.scenario.drones) {
				response += d.getId()+":"+d.getDroneRole()+",";
				
			}
			response = response.substring(0, response.length() - 1); 
		}
		else if (msg.contains("Drone.Action.SetRole.")){
			String strTargetDroneId = parts[3];
			String strTargetDroneRole = parts[4];
			int targetDroneId = Integer.parseInt(strTargetDroneId);

			System.out.println("Channel Received Message - Drone.Action.SetRole."+targetDroneId);
			for (Drone d : DroneLab.scenario.drones) {
				if (d.getId() == targetDroneId){
					switch (strTargetDroneRole) {
						case "RELAY":  d.setDroneRole(Drone.DroneRole.RELAY);
								 break;
						case "SOCIAL":  d.setDroneRole(Drone.DroneRole.SOCIAL);
								 break;
						case "ANTISOCIAL":  d.setDroneRole(Drone.DroneRole.ANTISOCIAL);
								 break;
					}
					//d.setDroneRole(Drone.DroneRole.ANTISOCIAL);
				}
			}
			response = "Drone.Action.SetRole."+strTargetDroneId+"."+strTargetDroneRole+"|Executed|";
		}

		/*else if (msg.contains("Drone.Action.SetRole.")){
			String strTargetDroneId = parts[3];
			String strTargetDroneRole = parts[4];
			int targetDroneId = Integer.parseInt(strTargetDroneId);
			System.out.println("Channel Received Message - Drone.Action.AddBehavior."+targetDroneId);
			Platform.runLater(
					() -> {
						setDroneRole(DroneRole role)
						d.getDroneRole()
						Utils.log("APPLIED SIM SETUP: " + flag.toLoadString() + " (" + sim.scenario.getNumVictims() + 
						" survivors); SOCIAL: " + numRole1 + 
						", RELAY: " + numRole2 + 
						", ANTI: " + numRole3 + 
						", WIFI: " + wifi_range +
						", QoK: " + DroneLab.operator.getQuality());
					// More detailed log
					for (Drone d : drones) {
						String s = "Drone: " + d.printBehaviors();
						Utils.log(s);
					}
						//DroneLab.scenario.processAddBehaviorForOneDrone(strTargetDroneRole,targetDroneId);
						System.out.println("Executed Addition of Behavior "+strTargetDroneRole+" for Drone "+strTargetDroneRole);
						}
					);

			response = "Drone.Action.AddBehavior."+strTargetDroneId+"."+strTargetDroneRole+"|Executed|";
		}*/
			

		for (Channel c : channels) {
			c.writeAndFlush(response);
		}


		
		//example of writing the message back to the all channels
		/*for (Channel c : channels) {
			c.writeAndFlush("-> " + msg + '\n');

		}
		*/
	}

	/*
	 * In case of exception, close channel. One may chose to custom handle exception
	 * & have alternative logical flows.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		System.out.println("Closing connection for client - " + ctx);
		System.out.println(cause);
		ctx.close();
	}
}

// Other code
/*
			for (Drone d : DroneLab.scenario.drones) {
				if (d.getId() == targetDroneId) {
					d.removeBehavior(Constants.STR_RELAY);
					Platform.runLater(
					() -> {
						DroneLab.sim.updateDroneParameterFields();
						}
					);
					
					//d.addBehavior("Launch");
					response = "Drone.Action.addBehavior."+strTargetDroneId+"."+strTargetDroneRole+"|Executed|";
			
					//response += d.getId()+":"+d.getDroneRole()+",";
				}
			}
			//response = response.substring(0, response.length() - 1); 	
		}*/