package com.mangecailloux.rube.loader.serializers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.ReadOnlySerializer;
import com.mangecailloux.rube.RubeDefaults;
import com.mangecailloux.rube.RubeScene;

public class BodySerializer extends ReadOnlySerializer<Body>
{
	private 	  World world;
	private RubeScene mScene;
	private final BodyDef def = new BodyDef();
	private final FixtureSerializer fixtureSerializer;

	public BodySerializer(Json json)
	{		
		fixtureSerializer = new FixtureSerializer(json);
		
		// as some Vector2 can be stored as a float we need a custom Vector2 Serializer :(
		json.setSerializer(Vector2.class, new Vector2Serializer());
		
		json.setSerializer(Fixture.class, fixtureSerializer);
	}
	
	public void setWorld(World _world)
	{
		world = _world;
	}
	
   public void setScene(RubeScene scene)
   {
      mScene = scene;
   }
	
	@Override
	public Body read(Json json, Object jsonData, Class type) 
	{
		if(world == null)
			return null;
		BodyDef defaults = RubeDefaults.Body.definition;

		int bodyType = json.readValue("type", int.class, defaults.type.getValue(), jsonData);
		
		if(bodyType == BodyType.DynamicBody.getValue())
			def.type = BodyType.DynamicBody;
		else if(bodyType == BodyType.KinematicBody.getValue())
			def.type = BodyType.KinematicBody;
		else
			def.type = BodyType.StaticBody;
		
		def.position.set(		json.readValue("position", 			Vector2.class, defaults.position, 		jsonData));
		def.linearVelocity.set(	json.readValue("linearVelocity", 	Vector2.class, defaults.linearVelocity, jsonData));
		
		def.angle 			= json.readValue("angle", 			float.class, defaults.angle, 			jsonData);
		def.angularVelocity = json.readValue("angularVelocity", float.class, defaults.angularVelocity, 	jsonData);
		def.linearDamping 	= json.readValue("linearDamping", 	float.class, defaults.linearDamping, 	jsonData);
		def.angularDamping 	= json.readValue("angularDamping", 	float.class, defaults.angularDamping, 	jsonData);
		def.gravityScale 	= json.readValue("gravityScale", 	float.class, defaults.gravityScale, 	jsonData);
		
		def.allowSleep 		= json.readValue("allowSleep", 		boolean.class, defaults.allowSleep, 	jsonData);
		def.awake 			= json.readValue("awake",			boolean.class, defaults.awake, 			jsonData);
		def.fixedRotation 	= json.readValue("fixedRotation", 	boolean.class, defaults.fixedRotation, 	jsonData);
		def.bullet 			= json.readValue("bullet", 			boolean.class, defaults.bullet, 		jsonData);
		def.active 			= json.readValue("active", 			boolean.class, defaults.active, 		jsonData);
		
		Body body = world.createBody(def);
		
		if(def.type == BodyType.DynamicBody)
		{
			Vector2 center = json.readValue("massData-center",  Vector2.class, jsonData);
			float mass = json.readValue("massData-mass", 	float.class, 0.0f, 	jsonData);
			float I = json.readValue("massData-I", 	float.class, 0.0f, 	jsonData);
			
			if(center != null)
			{
				MassData massData = new MassData();
				
				massData.center.set(center);
				massData.mass = mass;
				massData.I = I;
				
				if(massData.mass != 0.0f || massData.I != 0.0f || massData.center.x != 0.0f || massData.center.y != 0.0f)
					body.setMassData(massData);
			}
		}
		
		Array<Map<String,?>> customProperties = json.readValue("customProperties", Array.class, HashMap.class, jsonData);
		if (customProperties != null)
		{
		   for (int i = 0; i < customProperties.size; i++)
		   {
		      Map<String, ?> property = customProperties.get(i);
		      String propertyName = (String)property.get("name");
            if (property.containsKey("string"))
            {
               mScene.setCustom(body, propertyName, (String)property.get("string"));
            }
//		      else if (property.containsKey("int"))
//		      {
//		         setCustom(body, propertyName,(int)property.get("int"));
//		      }
//		      else if (property.containsKey("float"))
//		      {
//		         setCustom(body, propertyName, (float) property.get("float"));
//		      }

//		      else if (property.containsKey("Vec2"))
//		      {
//		         setCustom(body, propertyName, this.jsonToVec("Vec2", propValue));
//		      }
//		      else if (property.containsKey("bool"))
//		      {
//		         setCustom(body, propertyName, property.get("bool"));
//		      }
		   }
		}
		
		fixtureSerializer.setBody(body);
		json.readValue("fixture", Array.class, Fixture.class, jsonData);
		
		return body;
	}

}
