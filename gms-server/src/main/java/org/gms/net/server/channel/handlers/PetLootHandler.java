/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gms.net.server.channel.handlers;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.inventory.Pet;
import org.gms.net.AbstractPacketHandler;
import org.gms.net.packet.InPacket;
import org.gms.server.maps.MapItem;
import org.gms.server.maps.MapObject;
import org.gms.server.maps.MapObjectType;
import org.gms.util.PacketCreator;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author TheRamon
 * @author Ronan
 */
public final class PetLootHandler extends AbstractPacketHandler {
    @Override
    public final void handlePacket(InPacket p, Client c) {
        Character chr = c.getPlayer();

        int petIndex = chr.getPetIndex(p.readInt());
        Pet pet = chr.getPet(petIndex);
        if (pet == null || !pet.isSummoned()) {
            c.sendPacket(PacketCreator.enableActions());
            return;
        }

        if (pet.getLevel() > 10) {
            double fullnessPunishment = (1 - (pet.getFullness() / 100.0)) * 0.3;
            double range = ((pet.getLevel() - 5) * 40) * (1 - fullnessPunishment) + 50;
            List<MapObject> items = chr.getMap().getMapObjectsInRange(pet.getPos(),
                    range * range, Arrays.asList(MapObjectType.ITEM));

            // filter items that can be picked up
            List<MapObject> filteredItems = items.stream()
                    .filter(item -> {
                        MapItem mapItem = (MapItem) item;
                        return shouldPickupItem(chr, mapItem) && (mapItem.getOwnerId() == chr.getId()
                                || mapItem.getOwnerId() == chr.getPartyId());
                    })
                    .toList();

            chr.pickupItems(filteredItems, petIndex);
        } else {
            p.skip(13);
            int oid = p.readInt();
            MapObject ob = chr.getMap().getMapObject(oid);
            MapItem mapitem = (MapItem) ob;
            try {
                if (shouldPickupItem(chr, mapitem)) {
                    chr.pickupItem(mapitem, petIndex);
                    c.sendPacket(PacketCreator.enableActions());
                } else {
                    c.sendPacket(PacketCreator.enableActions());
                }
            } catch (NullPointerException e) {
                c.sendPacket(PacketCreator.enableActions());
                return;
            }
        }
    }

    private final boolean shouldPickupItem(Character chr, MapItem mapitem) {
        if (mapitem == null || mapitem.isPickedUp()) {
            return false;
        }
        if (mapitem.getMeso() > 0) {
            if (!chr.isEquippedMesoMagnet()) {
                return false;
            }

            if (chr.isEquippedPetItemIgnore()) {
                final Set<Integer> petIgnore = chr.getExcludedItems();
                if (!petIgnore.isEmpty() && petIgnore.contains(Integer.MAX_VALUE)) {
                    return false;
                }
            }
        } else {
            if (!chr.isEquippedItemPouch()) {
                return false;
            }

            if (chr.isEquippedPetItemIgnore()) {
                final Set<Integer> petIgnore = chr.getExcludedItems();
                if (!petIgnore.isEmpty() && petIgnore.contains(mapitem.getItem().getItemId())) {
                    return false;
                }
            }
        }
        return true;
    }
}
