package aqtclient.part;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import aqtclient.model.Tmaster;

@SuppressWarnings("unchecked")
public class AqtMain extends ApplicationWindow {
	static Composite container ;
	
	public static IAqtSetCode cback ;
	
	public static AuthType authtype = AuthType.USER;;

	EntityManager em ;
	final public static Color bluecol = SWTResourceManager.getColor(9,72,220);
	final public static Color htcol = SWTResourceManager.getColor(77,123,230);
	final public static Color forecol = SWTResourceManager.getColor(SWT.COLOR_WHITE);
	public static Tmaster tmaster = new Tmaster();
	public static EntityManagerFactory emf ;
	public String gv_tcode ;
	public static Timer jobScheduler ;
	TableViewer tv ;
	/**
	 * Create the application window.
	 */
	public AqtMain() {
		
		super(null);
//		createActions();
//		addToolBar(SWT.FLAT | SWT.COLOR_WHITE);
//		addMenuBar();
        String dbip = System.getProperty("AQTDB") ;  // 주소:port
        if (dbip == null) dbip = "localhost:3306" ;
        String sEtc = System.getProperty("AQTOPT") ;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("javax.persistence.jdbc.url", "jdbc:mariadb://" + dbip + "/aqtdb"  
        			+ (sEtc != null ? sEtc  :"") );
//        properties.put("javax.persistence.jdbc.user", "aqtdb");
//        properties.put("javax.persistence.jdbc.password", "Dawinit1!"); 
        properties.put("javax.persistence.jdbc.driver","org.mariadb.jdbc.Driver") ;
        
        emf = Persistence.createEntityManagerFactory("aqtclient", properties) ;
        em = AqtMain.emf.createEntityManager() ;
		System.out.println("AQT Started !!");
//		addStatusLine();
		
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
//		parent.setBackground(SWTResourceManager.getColor(234,240,234));
		parent.setBackground(SWTResourceManager.getColor(225,230,246));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setSashWidth(0);
		Composite comp1_1 = new Composite(sashForm, SWT.NONE) ;
		comp1_1.setBackground(parent.getBackground());
		comp1_1.setLayout(new GridLayout(1, true));
		
		menuCreate(comp1_1);

//		Label label = new Label(sashForm, SWT.SEPARATOR | SWT.VERTICAL);
//		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		container = new Composite(sashForm, SWT.NONE);
//		container.setBackground(parent.getBackground());
		container.setLayout(new FillLayout());
		
		new AqtResult(container, SWT.NONE);
		container.layout();
		container.setToolTipText("AqtResult");
		
//		sashForm.setWeights(new int[] {18,82});
		
		parent.addListener(SWT.Resize, new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				int w = 32000 / parent.getSize().x ; 

				sashForm.setWeights(new int[] {w ,100 - w});
				
			}
		});
//		parent.setSize(1850, 1000);

//		setStatus(container.getSize().toString());
		return container;
	}


	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		MenuManager menuManager = new MenuManager();
		
		menuManager.add(createFileMenu());
		return menuManager;
	}

	protected MenuManager createFileMenu() {
		MenuManager menu = new MenuManager("&File", "Id01");
//		ActionContributionItem acon = new ActionContributionItem(act1) ;
//		menu.add(act1);
//		
//		menu.add(actexit);
		menu.setVisible(false);
		return menu;
	}
	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		ToolBarManager toolBarManager = new ToolBarManager(style);

		return toolBarManager;
	}

	/**
	 * Create the status line manager.
	 * @return the status line manager
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		StatusLineManager statusLineManager = new StatusLineManager();
		return statusLineManager;
	}

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			AqtMain mainwin = new AqtMain();
			String pass = mainwin.em.createQuery("select c.pass1 from Tconfig c ", String.class).getSingleResult() ;
			AqtLogin aqtlogin  = new AqtLogin( mainwin.getParentShell(), pass ) ;
			if ( aqtlogin.open() == Window.OK) {

				mainwin.setBlockOnOpen(true);
				mainwin.open();
				Display.getCurrent().dispose();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Application Quarity Test");
		newShell.addListener(SWT.Close, new Listener() {
		      public void handleEvent(Event event) {
		        event.doit = true;
		        if (jobScheduler != null) jobScheduler.cancel();
		      }
		    });
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(1900, 1000);
	}

	/*
	 * private ImageRegistry iregistry ; public ImageDescriptor
	 * getImageDescriptor(String path) { if( iregistry == null ) { iregistry = new
	 * ImageRegistry(Display.getCurrent()); }
	 * 
	 * ImageDescriptor desc = iregistry.getDescriptor(path); if( desc == null ) {
	 * desc = ImageDescriptor.createFromFile(AqtMain.class, path);
	 * iregistry.put(path, desc); }
	 * 
	 * return desc; }
	 */
	public static void delWidget(Composite parent) {
		
		cback = null ;
		if (jobScheduler != null) jobScheduler.cancel();
	    for (Control kid : parent.getChildren()) {
	    	try {
		        kid.dispose();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}

	public static void openTrList(String cond_str) {

//		delWidget(container);
		AqtTRList trlist = new AqtTRList(container.getShell() , cond_str);
		trlist.open() ;
		
//		container.layout();
//		container.setToolTipText("AqtTRList");
	}
	private List<Tmaster> getTmaster() {
		em.getEntityManagerFactory().getCache().evictAll();
		return em.createQuery("select t from Tmaster t order by t.tdate desc", Tmaster.class).getResultList() ;
	}

	private void menuCreate(Composite parent ) {
		Image img_logo = SWTResourceManager.getImage( "images/logo.png");
		Image img_result = SWTResourceManager.getImage( "images/result.png");
		Image img_oper = SWTResourceManager.getImage( "images/operating.png");

		Color mcfore = SWTResourceManager.getColor(240,250,240) ;
		Composite comp_menu = new Composite(parent, SWT.NONE);
		
		GridLayout glayout = new GridLayout(1, false);
		glayout.marginLeft = 20;
		glayout.marginTop = 20 ;

		comp_menu.setLayout(glayout);
		comp_menu.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp_menu.setBackground(bluecol);
		

		Label lbl = new Label(comp_menu, SWT.NONE);
//		lblNewLabel.setLocation(40, 3);
//		lbl.setLayoutData(new GridData(GridData.FILL_BOTH));
		lbl.setImage(img_logo);
		lbl.setBackground(comp_menu.getBackground());

		lbl = new Label(comp_menu, SWT.NONE);
//		lbl.setLayoutData(new GridData(GridData.FILL_BOTH));
		lbl.setBackground(comp_menu.getBackground());
		
		lbl = new Label(comp_menu, SWT.NONE);
//		lbl.setLayoutData(new GridData(GridData.FILL_BOTH));
		lbl.setBackground(comp_menu.getBackground());
		lbl.setImage(img_result);

		Composite comp_1 = new Composite(comp_menu, SWT.NONE);
		GridLayout gl_4 = new GridLayout(1, false);
		gl_4.marginLeft = 20;
		gl_4.verticalSpacing = 15;
		gl_4.marginBottom = 20 ;
		
		comp_1.setLayout(gl_4);
//		gd_composite_4.heightHint = 50;
		comp_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		comp_1.setBackground(comp_menu.getBackground());


		

		Label lblist = new Label(comp_1, SWT.BOLD);
		menuLabel(lblist);
		lblist.setText("총괄현황");
		lblist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				delWidget(container);
				new AqtStatus(container, SWT.NONE);
				container.layout();
				container.setToolTipText("AqtStatus");
			}
		});

		lblist = new Label(comp_1, SWT.BOLD);
		menuLabel(lblist);
		lblist.setText("상세수행현황");
		lblist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (container.getToolTipText() == "AqtList" ) return;
				delWidget(container);
				new AqtList(container, SWT.NONE);
				container.layout();
				container.setToolTipText("AqtList");
			}
		});

		
		lblist = new Label(comp_1, SWT.BOLD);
		menuLabel(lblist);
		lblist.setText("상세수행결과");
		lblist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				delWidget(container);
				new AqtResult(container, SWT.NONE);
				try {
					cback.setTcode(gv_tcode);
				} catch (Exception e2) {
					// TODO: handle exception
				}
				container.layout();
				container.setToolTipText("AqtResult");
			}
		});

		lblist = new Label(comp_1, SWT.NONE);
		menuLabel(lblist);
		lblist.setText("수행결과비교");
		lblist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				delWidget(container);
				new AqtCompare(container, SWT.NONE);
				try {
					cback.setTcode(gv_tcode);
				} catch (Exception e2) {
					// TODO: handle exception
				}
				container.layout();
				container.setToolTipText("AqtCompare");

			}
		});

		lblist = new Label(comp_1, SWT.NONE);
		menuLabel(lblist);
		lblist.setText("전문처리현황");
		
		lblist.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				delWidget(container);
				container.setLayout(new FillLayout());
				new AqtView(container, SWT.NONE);
				try {
					cback.setTcode(gv_tcode);
				} catch (Exception e2) {
					// TODO: handle exception
				}
				container.layout();
				container.setToolTipText("AqtView");

			}
		});

//		Composite composite_12 = new Composite(comp_menu, SWT.NONE);
//		composite_12.setLayout(new GridLayout(1, false));
//		GridData gd_composite_12 = new GridData(SWT.FILL, SWT.CENTER, true, false);
//		gd_composite_12.heightHint = 50;
//		composite_12.setLayoutData(gd_composite_12);
//		composite_12.setBackground(comp_menu.getBackground());


		Label label = new Label(comp_menu, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		menuLabel(label);
//		label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		// configuration
		label = new Label(comp_menu, SWT.NONE);
		label.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		label.setImage(img_oper);


		comp_1 = new Composite(comp_menu, SWT.NONE);
		gl_4 = new GridLayout(1, false);
		gl_4.marginLeft = 20;
		gl_4.verticalSpacing = 15;
		gl_4.marginBottom = 20 ;
		
		comp_1.setLayout(gl_4);
//		gd_composite_4.heightHint = 50;
		comp_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		comp_1.setBackground(comp_menu.getBackground());
		// composite_8.setBackground(new Color (Display.getCurrent(), 159, 170, 222));

		{
			lbl = new Label(comp_1, SWT.NONE);

			menuLabel(lbl);
			lbl.setText("Step1.\n 테스트코드 등록");
			lbl.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent e) {
					AqtRegister aqtregister = new AqtRegister(container.getShell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM) ;
					aqtregister.setTmaster(new Tmaster());
					aqtregister.setTdate();
					aqtregister.open();
//					em.clear();
					tv.setInput( getTmaster() );
					

				}
			});

			lbl = new Label(comp_1, SWT.NONE);
			menuLabel(lbl);
			lbl.setText("Step2.\n 테스트전문송신");
			lbl.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseDown(MouseEvent e) {
					delWidget(container);
					new AqtExec(container, SWT.NONE);
					container.layout();
					container.setToolTipText("AqtRegister");
				}
			});
			
		}
		
		tv = new TableViewer(comp_menu, SWT.BORDER | SWT.NO_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION );
		
		tv.setUseHashlookup(true);

		Table tbl = tv.getTable();
		tbl.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				AqtRegister aqtregister = new AqtRegister(container.getShell(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM) ;
				aqtregister.setTmaster(tmaster);
				aqtregister.open();
				em.clear();
				tv.setInput( getTmaster() );
				try {
					cback.setTcode(gv_tcode);
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		});
		
		tbl.addListener(SWT.MouseWheel ,  new Listener() {
			
			@Override
			public void handleEvent(Event e) {
				e.doit = false ;
				if (e.count >= 0 )
					tbl.setTopIndex(tbl.getTopIndex() - 1);
				else
					tbl.setTopIndex(tbl.getTopIndex() + 1);
			}
		});
		
		tbl.addListener(SWT.MeasureItem,  new Listener() {
			
			@Override
			public void handleEvent(Event arg0) {
				arg0.height = (int)(arg0.gc.getFontMetrics().getHeight() * 1.5);
				
			}
		});
		tbl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem item = tbl.getItem(tbl.getSelectionIndex()) ;
				tmaster = ((Tmaster)item.getData())  ;
				gv_tcode = tmaster.getCode() ;
				if (cback != null ) cback.setTcode(gv_tcode);
			}
		});
		
//		tbl.setHeaderVisible(true);
		
//		tbl.setLinesVisible(true);
		tbl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tbl.setFont(SWTResourceManager.getFont( "맑은 고딕", 11, SWT.NORMAL));
		tbl.setBackground(comp_menu.getBackground());
		tbl.setForeground(mcfore);
		tbl.setCursor(IAqtVar.handc);
		
		tv.setUseHashlookup(true);		
		TableViewerColumn tvc = new TableViewerColumn(tv, SWT.CENTER);
		tvc.getColumn().setWidth(0);
		tvc.setLabelProvider(new ColumnLabelProvider() {
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}
			public String getText(Object element) {
				return null ;
			}
		});

		tvc = new TableViewerColumn(tv, SWT.CENTER);
		TableColumn tc1 = tvc.getColumn();
		tc1.setWidth(80);
		tc1.setText("Code");
		tc1.setAlignment(SWT.CENTER);

		tvc.setLabelProvider(new ColumnLabelProvider() {
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}
			public String getText(Object element) {
				if (element == null)  return "" ;
				return ((Tmaster)element).getCode()  ;
			}
		});

		tvc = new TableViewerColumn(tv, SWT.CENTER);
		TableColumn tc2 = tvc.getColumn();
		tc2.setWidth(180);
		tc2.setText("Description");
		tc2.setAlignment(SWT.LEFT);

		tvc.setLabelProvider(new ColumnLabelProvider() {
			public Image getImage(Object element) {
				// TODO Auto-generated method stub
				return null;
			}
			public String getText(Object element) {
				if (element == null)  return "" ;
				return ((Tmaster)element).getDesc1()  ;
			}
		});

		tv.setContentProvider(new IStructuredContentProvider() {
			
	    	@Override
	    	public Object[] getElements(Object input) {
	    		return ((List<Tmaster>)input).toArray();
	    	}
			
		});
		
		tv.setInput( getTmaster() );
		tv.refresh();
		tbl.select(0);
//		TableItem item = tbl.getItem(0);
		tmaster = ((Tmaster)tbl.getItem(0).getData() )  ;
		gv_tcode = tmaster.getCode() ;
		if (cback != null ) cback.setTcode(gv_tcode);

		Label ldawin = new Label(comp_menu, SWT.NONE);
		ldawin.setFont(SWTResourceManager.getFont( "Calibri", 12, SWT.ITALIC));
		ldawin.setText("CopyRight 2019 by DawinICT");
		ldawin.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		ldawin.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
		ldawin.setLayoutData(new GridData(SWT.CENTER,SWT.BOTTOM,true,true));

//		tbl.removeListener(SWT.MeasureItem, mlisten);
		
	}
	
	
	private void menuLabel(Label label) {
		label.setFont(SWTResourceManager.getFont( "맑은 고딕", 13, SWT.NORMAL));
		label.setCursor(IAqtVar.handc);
		label.setBackground(bluecol);
		label.setForeground(SWTResourceManager.getColor(240,250,240));
		
	}
	
    public static Image resize(Image image, int width, int height) {
		  Image scaled = new Image(Display.getCurrent(), width, height);
		  GC gc = new GC(scaled);
		  gc.setAntialias(SWT.ON);
		  gc.setInterpolation(SWT.HIGH);
		  gc.drawImage(image, 0, 0,image.getBounds().width, image.getBounds().height, 0, 0, width, height);
		  gc.dispose();
		  image.dispose(); // don't forget about me!
		  return scaled;
	}
    
    public static void exportTable(TableViewer tableViewer, int si)  {
        // TODO: add logic to ask user for the file location

    	String[] ext = { "csv" }  ;
    	   final FileDialog dlg = new FileDialog ( Display.getDefault().getActiveShell() , SWT.APPLICATION_MODAL | SWT.SAVE );
    	    dlg.setFileName("datname");
    	    dlg.setFilterExtensions ( ext );
    	    dlg.setOverwrite ( true );
    	    dlg.setText ( "저장파일명 선택" );

    	    String fileName = dlg.open ();
    	    if ( fileName == null )
    	    {
    	        return ;
    	    }
    	    if (!fileName.matches("\\.csv$") ) fileName += ".csv" ;
//      File  file = new File(fileName + "." + ext[ dlg.getFilterIndex() ] );
    	    
    	    
//        BufferedWriter bw = new BufferedWriter(osw);
        
        try {
    		FileOutputStream fos = new FileOutputStream(fileName );
    		OutputStreamWriter osw = new OutputStreamWriter(fos, "MS949");
        	BufferedWriter writer = new BufferedWriter(new BufferedWriter(osw)) ;
            final Table table = tableViewer.getTable();
            final int[] columnOrder = table.getColumnOrder();
            final int colcnt = table.getColumnCount() ;
            for(int columnOrderIndex = si; columnOrderIndex < colcnt; 
                    columnOrderIndex++) {
                int columnIndex = columnOrder[columnOrderIndex];
                TableColumn tableColumn = table.getColumn(columnIndex);
                if (tableColumn.getText().equals("ID")) 
                	writer.write("SID");
                else
                    writer.write(tableColumn.getText());
                if ( columnOrderIndex+1 < colcnt ) writer.write(",");
            }
            writer.write("\r\n");
            
            final int itemCount = table.getItemCount();
            for(int itemIndex = 0; itemIndex < itemCount; itemIndex++) {
                TableItem item = table.getItem(itemIndex);
                
                for(int columnOrderIndex = si; 
                        columnOrderIndex < colcnt; 
                        columnOrderIndex++) {
                    int columnIndex = columnOrder[columnOrderIndex];
                    writer.write(item.getText(columnIndex));
                    if ( columnOrderIndex+1 < colcnt ) writer.write(",");
                }
                writer.write("\r\n");
            }
            writer.close();
        } catch(IOException ioe) {
            // TODO: add logic to inform the user of the problem
            System.err.println("trouble exporting table data to file");
            ioe.printStackTrace();
		}
        
    }

}
