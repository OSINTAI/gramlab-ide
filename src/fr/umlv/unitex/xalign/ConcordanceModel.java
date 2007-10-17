/*
 * Unitex
 *
 * Copyright (C) 2001-2007 Universit� Paris-Est Marne-la-Vall�e <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */
package fr.umlv.unitex.xalign;

import javax.swing.*;

public interface ConcordanceModel extends ListModel {

	/**
	 * These constants are used to set the display mode:
	 * TEXT: all sentences are shown, with plain text
	 * MATCHES: only matched sentences are shown, with HTML
	 * BOTH: all sentences are shown, with HTML for matched sentences
	 */
	public final static int TEXT=0;
	public final static int MATCHES=1;
	public final static int BOTH=2;
	
	public void addMatch(int sentence,Occurrence match);
	public boolean isMatchedSentenceNumber(int sentence);
	public boolean isMatchedSentenceIndex(int index);
	public void setMode(int mode);
	public int getMode();
	public XMLTextModel getModel();
	public int getSentence(int index);
	public int getSentenceIndex(int sentence);
	public void refresh();
	
}