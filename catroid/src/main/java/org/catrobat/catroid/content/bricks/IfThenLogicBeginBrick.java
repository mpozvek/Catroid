/*
 * Catroid: An on-device visual programming system for Android devices
 * Copyright (C) 2010-2018 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * An additional term exception under section 7 of the GNU Affero
 * General Public License, version 3, is available at
 * http://developer.catrobat.org/license_additional_term
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.catroid.content.bricks;

import android.content.Context;
import android.view.View;

import com.badlogic.gdx.scenes.scene2d.Action;

import org.catrobat.catroid.R;
import org.catrobat.catroid.content.ActionFactory;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ScriptSequenceAction;
import org.catrobat.catroid.formulaeditor.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IfThenLogicBeginBrick extends FormulaBrick implements CompositeBrick {

	private static final long serialVersionUID = 1L;

	private transient EndBrick endBrick = new EndBrick(this);

	private List<Brick> ifBranchBricks = new ArrayList<>();

	public IfThenLogicBeginBrick() {
		addAllowedBrickField(Brick.BrickField.IF_CONDITION, R.id.brick_if_begin_edit_text);
	}

	public IfThenLogicBeginBrick(Formula formula) {
		this();
		setFormulaWithBrickField(Brick.BrickField.IF_CONDITION, formula);
	}

	@Override
	public boolean hasSecondaryList() {
		return false;
	}

	@Override
	public List<Brick> getNestedBricks() {
		return ifBranchBricks;
	}

	@Override
	public List<Brick> getSecondaryNestedBricks() {
		return null;
	}

	public boolean addBrick(Brick brick) {
		return ifBranchBricks.add(brick);
	}

	@Override
	public void setCommentedOut(boolean commentedOut) {
		super.setCommentedOut(commentedOut);
		for (Brick brick : ifBranchBricks) {
			brick.setCommentedOut(commentedOut);
		}
	}

	@Override
	public Brick clone() throws CloneNotSupportedException {
		IfThenLogicBeginBrick clone = (IfThenLogicBeginBrick) super.clone();
		clone.endBrick = new EndBrick(clone);
		clone.ifBranchBricks = new ArrayList<>();
		for (Brick brick : ifBranchBricks) {
			clone.addBrick(brick.clone());
		}
		return clone;
	}

	@Override
	public boolean consistsOfMultipleParts() {
		return true;
	}

	@Override
	public List<Brick> getAllParts() {
		List<Brick> bricks = new ArrayList<>();
		bricks.add(this);
		bricks.add(endBrick);
		return bricks;
	}

	@Override
	public void addToFlatList(List<Brick> bricks) {
		super.addToFlatList(bricks);
		for (Brick brick : ifBranchBricks) {
			brick.addToFlatList(bricks);
		}
		bricks.add(endBrick);
	}

	@Override
	public void setParent(Brick parent) {
		super.setParent(parent);
		for (Brick brick : ifBranchBricks) {
			brick.setParent(this);
		}
	}

	@Override
	public List<Brick> getDragAndDropTargetList() {
		return ifBranchBricks;
	}

	@Override
	public boolean removeChild(Brick brick) {
		if (ifBranchBricks.remove(brick)) {
			return true;
		}
		for (Brick childBrick : ifBranchBricks) {
			if (childBrick.removeChild(brick)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getViewResource() {
		return R.layout.brick_if_then_begin_if;
	}

	@Override
	public View getPrototypeView(Context context) {
		View view = super.getPrototypeView(context);
		view.findViewById(R.id.if_prototype_punctuation).setVisibility(View.VISIBLE);
		return view;
	}

	@Override
	public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
		ScriptSequenceAction ifSequence = (ScriptSequenceAction) ActionFactory.createScriptSequenceAction(sequence.getScript());

		for (Brick brick : ifBranchBricks) {
			if (!brick.isCommentedOut()) {
				brick.addActionToSequence(sprite, ifSequence);
			}
		}

		Action action = sprite.getActionFactory()
				.createIfLogicAction(sprite, sequence,
						getFormulaWithBrickField(Brick.BrickField.IF_CONDITION), ifSequence, null);

		sequence.addAction(action);
	}

	@Override
	public void addRequiredResources(final ResourcesSet requiredResourcesSet) {
		super.addRequiredResources(requiredResourcesSet);
		for (Brick brick : ifBranchBricks) {
			brick.addRequiredResources(requiredResourcesSet);
		}
	}

	private static class EndBrick extends BrickBaseType {

		EndBrick(IfThenLogicBeginBrick parent) {
			this.parent = parent;
		}

		@Override
		public boolean isCommentedOut() {
			return parent.isCommentedOut();
		}

		@Override
		public boolean consistsOfMultipleParts() {
			return true;
		}

		@Override
		public List<Brick> getAllParts() {
			return parent.getAllParts();
		}

		@Override
		public void addToFlatList(List<Brick> bricks) {
			parent.addToFlatList(bricks);
		}

		@Override
		public List<Brick> getDragAndDropTargetList() {
			return parent.getParent().getDragAndDropTargetList();
		}

		@Override
		public int getPositionInDragAndDropTargetList() {
			return parent.getParent().getDragAndDropTargetList().indexOf(parent);
		}

		@Override
		public int getViewResource() {
			return R.layout.brick_if_end_if;
		}

		@Override
		public void addActionToSequence(Sprite sprite, ScriptSequenceAction sequence) {
		}

		@Override
		public UUID getBrickID() {
			return parent.getBrickID();
		}
	}
}
