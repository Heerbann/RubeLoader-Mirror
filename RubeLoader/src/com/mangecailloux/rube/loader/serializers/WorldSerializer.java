package com.mangecailloux.rube.loader.serializers;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.mangecailloux.rube.RubeDefaults;
import com.mangecailloux.rube.RubeScene;

public class WorldSerializer extends ReadOnlySerializer<World>
{
	private final BodySerializer 	bodySerializer;
	private final JointSerializer 	jointSerializer;
	
	private RubeScene mScene;
	
	public WorldSerializer(Json _json)
	{
		bodySerializer = new BodySerializer(_json);
		_json.setSerializer(Body.class, bodySerializer);
		
		jointSerializer = new JointSerializer(_json);
		_json.setSerializer(Joint.class, jointSerializer);
	}
	
	public void setScene(RubeScene scene)
	{
	   mScene = scene;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public World read(Json json, Object jsonData, Class type) 
	{
		boolean allowSleep = json.readValue("allowSleep", boolean.class, RubeDefaults.World.allowSleep, jsonData);
		boolean autoClearForces = json.readValue("autoClearForces", boolean.class, RubeDefaults.World.autoClearForces, jsonData);
		boolean continuousPhysics = json.readValue("continuousPhysics", boolean.class, RubeDefaults.World.continuousPhysics, jsonData);
		boolean warmStarting = json.readValue("warmStarting", boolean.class, RubeDefaults.World.warmStarting, jsonData);
		
		Vector2 gravity = json.readValue("gravity", Vector2.class, RubeDefaults.World.gravity, jsonData);
		
		World world = new World(gravity, allowSleep);
		world.setAutoClearForces(autoClearForces);
		world.setContinuousPhysics(continuousPhysics);
		world.setWarmStarting(warmStarting);
		
		// Bodies
		bodySerializer.setWorld(world);
		bodySerializer.setScene(mScene);
		Array<Body> bodies = json.readValue("body", Array.class, Body.class, jsonData);
		mScene.setBodies(bodies);
		
		// Joints
		// joints are done in two passes because gear joints reference other joints
		// First joint pass
		jointSerializer.init(world, bodies, null);
		Array<Joint> joints = json.readValue("joint", Array.class, Joint.class, jsonData);
		// Second joint pass
		jointSerializer.init(world, bodies, joints);
		joints = json.readValue("joint", Array.class, Joint.class, jsonData);
		mScene.setJoints(joints);
		
		return world;
	}

}
