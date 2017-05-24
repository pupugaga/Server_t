package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.ai.CtrlIntention;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ShowTownMap;
import net.sf.l2j.gameserver.network.serverpackets.StaticObjectInfo;

/**
 * GODSON ROX!
 */
public class StaticObject extends WorldObject
{
	private int _staticObjectId;
	private int _type = -1; // 0 - map signs, 1 - throne , 2 - arena signs
	private boolean _isBusy; // True - if someone sitting on the throne
	private ShowTownMap _map;
	
	public StaticObject(int objectId)
	{
		super(objectId);
	}
	
	/**
	 * @return the StaticObjectId.
	 */
	public int getStaticObjectId()
	{
		return _staticObjectId;
	}
	
	/**
	 * @param StaticObjectId The StaticObjectId to set.
	 */
	public void setStaticObjectId(int StaticObjectId)
	{
		_staticObjectId = StaticObjectId;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public void setType(int type)
	{
		_type = type;
	}
	
	public boolean isBusy()
	{
		return _isBusy;
	}
	
	public void setBusy(boolean busy)
	{
		_isBusy = busy;
	}
	
	public void setMap(String texture, int x, int y)
	{
		_map = new ShowTownMap("town_map." + texture, x, y);
	}
	
	public ShowTownMap getMap()
	{
		return _map;
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the player
		if (player.getTarget() != this)
			player.setTarget(this);
		else
		{
			// Calculate the distance between the Player and the L2Npc
			if (!player.isInsideRadius(this, Npc.INTERACTION_DISTANCE, false, false))
			{
				// Notify the Player AI with INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				if (getType() == 2)
				{
					final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/signboard.htm");
					player.sendPacket(html);
				}
				else if (getType() == 0)
					player.sendPacket(getMap());
				
				// Send ActionFailed to the player in order to avoid he stucks
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (player.isGM())
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/admin/staticinfo.htm");
			html.replace("%x%", getX());
			html.replace("%y%", getY());
			html.replace("%z%", getZ());
			html.replace("%objid%", getObjectId());
			html.replace("%staticid%", getStaticObjectId());
			html.replace("%class%", getClass().getSimpleName());
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		if (player.getTarget() != this)
			player.setTarget(this);
		else
			player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return false;
	}
	
	@Override
	public void sendInfo(Player activeChar)
	{
		activeChar.sendPacket(new StaticObjectInfo(this));
	}
}