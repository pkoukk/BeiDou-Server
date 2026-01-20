/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
    Copyleft (L) 2016 - 2019 RonanLana

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

/*
   @Author: Arthur L - Refactored command content into modules
*/
package org.gms.client.command.commands.gm0;

import java.util.ArrayList;

import org.gms.client.Character;
import org.gms.client.Client;
import org.gms.client.Job;
import org.gms.client.SkillFactory;
import org.gms.client.command.Command;
import org.gms.util.I18nUtil;

public class BuyBuffCommand extends Command {
    {
        setDescription(I18nUtil.getMessage("BuyBuffCommand.message1"));
    }

    @Override
    public void execute(Client c, String[] params) {
        Character player = c.getPlayer();
        int mesoCost=0;
        ArrayList<Integer> buffList = new ArrayList<Integer>();
        if (player.getLevel()>=20) {
            buffList.add(4101004); // 轻功
            mesoCost+=500;
        } 
        if (player.getLevel()>=30) {
            buffList.add(2311003); // 神圣祈祷（花）
            mesoCost+=5000;
        } 
        if (player.getLevel()>=30) {
            buffList.add(1301007); // 神圣之火
            mesoCost+=5000;
        }
        if (player.getLevel()>=50) {
            buffList.add(4111001); // 聚财术
            mesoCost+=10000;
            if (player.getJob().isA(Job.MAGICIAN)){
                buffList.add(1111007); // 防御崩坏
                mesoCost+=10000;
            }
        } 
        if (player.getLevel()>=100) {
            buffList.add(3121002); // 火眼晶晶
            mesoCost+=50000;
        }         
        if (player.getLevel()>=100) {
            buffList.add(5121009); // 极速领域
            mesoCost+=50000;
        } 
        
        if (player.getMeso() < mesoCost) {
            player.yellowMessage(I18nUtil.getMessage("BuyBuffCommand.message2") + mesoCost);
            return;
        }else{
            player.gainMeso(-mesoCost, true);
            player.yellowMessage(I18nUtil.getMessage("BuyBuffCommand.message3") + mesoCost);
        }

        for (Integer skillId : buffList) {
            SkillFactory.getSkill(skillId).getEffect(SkillFactory.getSkill(skillId).getMaxLevel()).applyTo(player);
        }
        
    }
}
