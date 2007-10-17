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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import fr.umlv.unitex.*;
import fr.umlv.unitex.process.*;

public class XAlignFrame {

	static Font sourceFont;
	static Font targetFont;
	static {
		JTextPane foo=new JTextPane();
		sourceFont=foo.getFont();
		targetFont=foo.getFont();
	}
	
	public static JInternalFrame frame;
	public static File alignementFile;
	static XMLTextModel text1,text2;
	static XAlignModel model;
	
	
	public static JInternalFrame buildAlignFrame(final File f1,final File f2,final File align) throws IOException {
		alignementFile=align;
		tryToFindFonts(f1,f2);
		frame=new JInternalFrame("XAlign",true,true);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setSize(800,600);
		/* First text */
		MappedByteBuffer buffer1=XMLTextLoader.buildMappedByteBuffer(f1);
		text1=new XMLTextModelImpl(buffer1);
		XMLTextLoader loader1=new XMLTextLoader(text1,buffer1);
		loader1.load();
		ConcordanceModel model1=new ConcordanceModelImpl(text1);
		ConcordanceLoader.load(new File("D:\\xalign\\concord-align.ind"),model1);
		/* Second text */
		MappedByteBuffer buffer2=XMLTextLoader.buildMappedByteBuffer(f2);
		text2=new XMLTextModelImpl(buffer2);
		XMLTextLoader loader2=new XMLTextLoader(text2,buffer2);
		loader2.load();
		ConcordanceModel model2=new ConcordanceModelImpl(text2);
		ConcordanceLoader.load(new File("D:\\xalign\\concord-align2.ind"),model2);
		model=new XAlignModelImpl(text1,text2);
		model.load(align);
		model.addAlignmentListener(new AlignmentListener() {
			public void alignmentChanged(AlignmentEvent e) {
				if (AlignmentEvent.MANUAL_EDIT.equals(e)) {
					frame.setTitle("XAlign (alignment modified)");
				} else if (AlignmentEvent.SAVING.equals(e)) {
					frame.setTitle("XAlign");
				} 
			}
		});
		frame.addInternalFrameListener(new InternalFrameListener() {

			public void internalFrameOpened(InternalFrameEvent e) {/* */}

			public void internalFrameClosing(InternalFrameEvent e) {
				if (model.isModified()) {
					Object[] options_on_exit = { "Save", "Don't save" };
					Object[] normal_options = { "Save", "Don't save", "Cancel" };
					int n;
					if (UnitexFrame.closing) {
						n = JOptionPane
								.showOptionDialog(
										frame,
										"Alignment has been modified. Do you want to save it ?",
										"", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null,
										options_on_exit, options_on_exit[0]);
					} else {
						n = JOptionPane
								.showOptionDialog(
										frame,
										"Alignment has been modified. Do you want to save it ?",
										"", JOptionPane.YES_NO_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null,
										normal_options, normal_options[0]);
					}
					if (n == JOptionPane.CLOSED_OPTION)
						return;
					if (n == 0) {
						XAlignFrame.saveAlignment(model);
					}
					if (n != 2) {
						frame.setVisible(false);
						frame.dispose();
						text1.reset();
						text2.reset();
						model.reset();
						return;
					}
					frame.setVisible(true);
					try {
						frame.setSelected(true);
						frame.setIcon(false);
					} catch (java.beans.PropertyVetoException e2) {
						e2.printStackTrace();
					}
					return;
				}
				text1.reset();
				text2.reset();
				model.reset();
				frame.setVisible(false);
				UnitexFrame.removeInternalFrame(frame);
			}

			public void internalFrameClosed(InternalFrameEvent e) {/* */} 
			public void internalFrameIconified(InternalFrameEvent e) {/* */}
			public void internalFrameDeiconified(InternalFrameEvent e) {/* */}
			public void internalFrameActivated(InternalFrameEvent e) {/* */}
			public void internalFrameDeactivated(InternalFrameEvent e) {/* */}
		});
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new XAlignPane(model1,model2,model),BorderLayout.CENTER);
		
		JPanel radioPanel1=createRadioPanel(model1,true);
		JPanel radioPanel2=createRadioPanel(model2,false);
		JButton alignButton=new JButton("Align");
		alignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						XAlignFrame.saveAlignment(model);
						if (alignementFile==null || model.isModified()) {
							/* If the user hasn't saved the alignment */
							return;
						}
						File alignmentProperties=Config.getAlignmentProperties();
						XAlignCommand cmd=new XAlignCommand();
						cmd=cmd.source(f1).target(f2).properties(alignmentProperties)
							.alignment(alignementFile);
						new ProcessInfoFrame(cmd,true,new XAlignDo(model,alignementFile));
					}});
			}});
		JButton saveButton=new JButton("Save alignment");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						XAlignFrame.saveAlignment(model);
					}});
			}});
		JButton saveAsButton=new JButton("Save alignment as...");
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						XAlignFrame.saveAlignmentAs(model);
					}});
			}});
		JPanel downPanel=new JPanel(new BorderLayout());
		downPanel.add(radioPanel1,BorderLayout.WEST);
		downPanel.add(radioPanel2,BorderLayout.EAST);
		JPanel buttonPanel=new JPanel();
		buttonPanel.add(alignButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveAsButton);
		downPanel.add(buttonPanel,BorderLayout.SOUTH);
		downPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		frame.getContentPane().add(downPanel,BorderLayout.SOUTH);
		return frame;
	}

	protected static void saveAlignment(XAlignModel model1) {
		if (alignementFile!=null) {
			saveAlignment(alignementFile,model1);
		} else {
			saveAlignmentAs(model1);
		}
	}

	static void saveAlignmentAs(XAlignModel model1) {
		JFileChooser chooser=AlignmentParameterFrame.alignmentChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		int returnVal = chooser.showSaveDialog(frame);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		alignementFile = chooser.getSelectedFile();
		saveAlignment(alignementFile,model1);
	}

	private static void saveAlignment(File alignementFile1,XAlignModel model1) {
		try {
			model1.dumpAlignments(alignementFile1);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void tryToFindFonts(File f1, File f2) {
		Font f=tryToFindFont(f1);
		if (f!=null) {
			sourceFont=f;
		}
		f=tryToFindFont(f2);
		if (f!=null) {
			targetFont=f;
		}
	}

	/**
	 * This method tries to determine the language of the given file, on
	 * the basis of its path:
	 * 
	 * ..../Unitex/Thai/...../foo.xml => Thai
	 * ..../my unitex/French/...../foo.xml => French
	 */
	private static Font tryToFindFont(File f) {
		Font font=tryToFindFont(f.getParentFile(),Config.getUnitexDir());
		if (font==null) {
			font=tryToFindFont(f.getParentFile(),Config.getUserDir());
		}
		return font;
	}

	/**
	 * f is the directory containing the XML file.
	 */
	private static Font tryToFindFont(File f,File dir) {
		if (f==null) {
			return null;
		}
		if (f.equals(dir)) {
			/* If we are at the root of the directory, we can't
			 * say anything about the language.
			 */
			return null;
		}
		while (f.getParentFile()!=null && !f.getParentFile().equals(dir)) {
			f=f.getParentFile();
		}
		if (f.getParentFile()==null) {
			/* We were not in the directory */
			return null;
		}
		/* Here, we have found a language directory */
		String name=f.getName();
		if (name.equals("Users") || name.equals("App") ||
				name.equals("Src") || name.equals("XAlign")) {
			return null;
		}
		/* Now, we will look into the config file which is the preferred font
		 * for this language */
		File config=new File(f,"Config");
		if (!config.exists()) {
			return null;
		}
		Properties prop=Preferences.loadProperties(config,null);
		String s=prop.getProperty("TEXT FONT SIZE");
		if (s==null) return null;
		int fontSize = Integer.parseInt(s);
		s=prop.getProperty("TEXT FONT NAME");
		if (s==null) return null;
		Font font = new Font(s,Font.PLAIN,(int)(fontSize/0.72));
		return font;
	}

	/**
	 * This method tries to determine the language of the given file, on
	 * the basis of its path:
	 * 
	 * ..../Unitex/Thai/...../foo.xml => Thai
	 * ..../my unitex/French/...../foo.xml => French
	 */
	public static File tryToFindAlphabet(File f) {
		File file=tryToFindAlphabet(f.getParentFile(),Config.getUnitexDir());
		if (file==null) {
			file=tryToFindAlphabet(f.getParentFile(),Config.getUserDir());
		}
		return file;
	}

	/**
	 * f is the directory containing the XML file.
	 */
	private static File tryToFindAlphabet(File f,File dir) {
		if (f==null) {
			return null;
		}
		if (f.equals(dir)) {
			/* If we are at the root of the directory, we can't
			 * say anything about the language.
			 */
			return null;
		}
		while (f.getParentFile()!=null && !f.getParentFile().equals(dir)) {
			f=f.getParentFile();
		}
		if (f.getParentFile()==null) {
			/* We were not in the directory */
			return null;
		}
		/* Here, we have found a language directory */
		String name=f.getName();
		if (name.equals("Users") || name.equals("App") ||
				name.equals("Src") || name.equals("XAlign")) {
			return null;
		}
		/* Now, we will look into the config file which is the preferred font
		 * for this language */
		File alphabet=new File(f,"Alphabet.txt");
		if (!alphabet.exists()) {
			return null;
		}
		return alphabet;
	}

	private static JPanel createRadioPanel(final ConcordanceModel model1,boolean left) {
		JPanel p=new JPanel(new GridLayout(3,1));
		String[] captions={"All sentences/Plain text","Matched sentences","All sentences/HTML"};
		int[] modes={ConcordanceModel.TEXT,ConcordanceModel.MATCHES,ConcordanceModel.BOTH};
		ButtonGroup g=new ButtonGroup();
		for (int i=0;i<captions.length;i++) {
			final JRadioButton b=new JRadioButton(captions[i],i==0);
			if (!left) {
				b.setHorizontalTextPosition(SwingConstants.LEFT);
				b.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
			}
			final int mode=modes[i];
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (b.isSelected()) {
						model1.setMode(mode);
					}
				}});
			g.add(b);
			p.add(b);
		}
		if (!left) {
			p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
		return p;
	}
	
	
	static class XAlignDo extends ToDoAbstract {
		
		XAlignModel model1;
		File f;
		
		XAlignDo(XAlignModel model,File f) {
			this.model1=model;
			this.f=f;
		}
		
		public void toDo() {
			model1.load(f);
		}
	}
}