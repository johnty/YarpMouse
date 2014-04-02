//--------------------------------------------------------------------------------------
//
//  File:       YarpMouse.java
//
//  Project:    YarpMouse
//
//  Contains:   Simple processing sketch that sends mouse drag position and clicks via a yarp port
//
//  Written by: Johnty Wang
//
//  Copyright:  (c) 2014 by HPlus Technologies Ltd. and Simon Fraser University.
//
//              All rights reserved. Redistribution and use in source and binary forms,
//              with or without modification, are permitted provided that the following
//              conditions are met:
//                * Redistributions of source code must retain the above copyright
//                  notice, this list of conditions and the following disclaimer.
//                * Redistributions in binary form must reproduce the above copyright
//                  notice, this list of conditions and the following disclaimer in the
//                  documentation and/or other materials provided with the
//                  distribution.
//                * Neither the name of the copyright holders nor the names of its
//                  contributors may be used to endorse or promote products derived
//                  from this software without specific prior written permission.
//
//              THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//              "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//              LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
//              PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
//              OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//              SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
//              LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//              DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
//              THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
//              (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//              OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//  Created:    2014-03-15
//
//--------------------------------------------------------------------------------------
 
import processing.core.*;
import yarp.BufferedPortBottle;
import yarp.Port;
import yarp.Bottle;


public class YarpMouse extends PApplet {

	/**
	 * Processing app for remote mouse cursor; left click and drag position.
	 * sends /mouse X Y when mouse dragged
	 * sends "/mouse up" or "/mouse down" when mouse button is up/down
	 * opens a port called "/yarpMouse" and sends everything through it.
	 * Johnty Wang 2014 johntywang@gmail.com
	 * HPlusTech
	 */
	
	final static int screen_width = 640;
	final static int screen_height = 480;
	private BufferedPortBottle port;
	private PFont font;
	private int mX;
	private int mY;
	private boolean mDown;
	
	private static final long serialVersionUID = 1L;
	
	private static final String portName = "/yarpMouse";

	public static void main(String[] args) {
		System.loadLibrary("jyarp");
		yarp.Network.init();
		PApplet.main(new String[] {"YarpMouse"}); //windowed mode

	}
	
	public void setup() {
		font = loadFont("Tahoma-32.vlw");
		textFont(font, 32);

		size(screen_width, screen_height);
		background(0);
		port = new BufferedPortBottle();
		println("opening yarp port "+ portName);
		port.open(portName);
		yarp.Network.connect(portName, "/ofxgvf");
		mX = 0;
		mY = 0;
		mDown = false;

	}

	public void draw() {

		background(0);	
		fill(255);
		String str = "yarp port at "+ port.getName() + "; press esc to quit";
		text(str, 10, 25);
		if (mDown)
			fill(255,0,0);
		else
			fill(255);
		str = "sending pointer location "+ nf((float)mX/screen_width,1,2) + ":" + nf((float)mY/screen_height,1,2);
		text(str, 10, 75);
		
		stroke(255);
	    if (mousePressed) {
	    	ellipse(mouseX,mouseY,25,25);
	    }
	}

	public void keyPressed() {
		if (key == ESC) {
			println("closing port...");
			port.close();
			exit();
		}
	}

	public void mouseDragged() {
		mX = mouseX;
		mY = mouseY;
		conditionVals();
		sendBotXY();

	}

	public void mousePressed() {
		mX = mouseX;
		mY = mouseY;
		mDown = true;
		conditionVals();
		sendBotBtn(mDown);
		
	}

	public void mouseReleased() {
		mX = mouseX;
		mY = mouseY;
		mDown = false;
		conditionVals();
		sendBotBtn(mDown);
	}
	
	private void conditionVals() {
		//check values and clip them if necessary
		if (mX < 0) mX = 0;
		if (mY < 0) mY = 0;
		if (mX > screen_width) mX = screen_width;
		if (mY > screen_height) mY = screen_height;
	}
	
	private void sendBotXY() {
		//update and send bottle
		while (port.isWriting()) {
			//println("waitSnd");
		}
		print(".");
		Bottle bot = new Bottle();
		bot = port.prepare();
		bot.clear();
		bot.addString("/mouse");
		bot.addDouble((float)mX/screen_width);
		bot.addDouble((float)mY/screen_height);

		port.write();
	}
	
	private void sendBotBtn(boolean isDown) {
		while (port.isWriting()) {
			//println("waitBtn");
		}
		Bottle bot = new Bottle();
		bot = port.prepare();
		bot.clear();
		bot.addString("/mouse");
		if (isDown) {
			bot.addString("down");
			println("down");
		}
		else {
			bot.addString("up");
			println("up");
		}
		bot.addDouble((float)mX/screen_width);
		bot.addDouble((float)mY/screen_height);
		port.write();
		//this delay is a bit of a hack, for the way
		// i'm using yarp receive has some issues on the other end
		// if sending from here was synchronized, it would avoid this...
		try {
			Thread.sleep(25);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}


}
