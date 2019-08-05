package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = false;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, false))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        anywheresoftware.b4a.keywords.Common.ToastMessageShow("This application was developed with B4A trial version and should not be distributed.", true);
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b1 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b4 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b7 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bpoint = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b5 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b8 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b0 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b3 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b6 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _b9 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bequal = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bplus = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bdivision = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bmul = null;
public anywheresoftware.b4a.objects.ButtonWrapper _bmoins = null;
public anywheresoftware.b4a.objects.ButtonWrapper _beffacer = null;
public anywheresoftware.b4a.objects.ButtonWrapper _faco = null;
public static String _affiche = "";
public anywheresoftware.b4a.objects.LabelWrapper _edit1 = null;
public static double _chiffre = 0;
public static int _chiffreref = 0;
public static double _memo = 0;
public static int _test = 0;
public b4a.example.starter _starter = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
return vis;}
public static String  _action_click() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _virtual = null;
 //BA.debugLineNum = 67;BA.debugLine="Sub action_Click";
 //BA.debugLineNum = 68;BA.debugLine="affiche =  Edit1.Text";
mostCurrent._affiche = mostCurrent._edit1.getText();
 //BA.debugLineNum = 69;BA.debugLine="If IsNumber (affiche) Then chiffre = affiche";
if (anywheresoftware.b4a.keywords.Common.IsNumber(mostCurrent._affiche)) { 
_chiffre = (double)(Double.parseDouble(mostCurrent._affiche));};
 //BA.debugLineNum = 71;BA.debugLine="Dim virtual As Button";
_virtual = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 72;BA.debugLine="virtual = Sender";
_virtual.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 73;BA.debugLine="Edit1.Text = \"\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(""));
 //BA.debugLineNum = 74;BA.debugLine="Select virtual.Text";
switch (BA.switchObjectToInt(_virtual.getText(),"C","+","*","-","/","=",".")) {
case 0: {
 //BA.debugLineNum = 76;BA.debugLine="Edit1.Text=Null";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 77;BA.debugLine="affiche=Null";
mostCurrent._affiche = BA.ObjectToString(anywheresoftware.b4a.keywords.Common.Null);
 //BA.debugLineNum = 78;BA.debugLine="Edit1.Tag= Null";
mostCurrent._edit1.setTag(anywheresoftware.b4a.keywords.Common.Null);
 //BA.debugLineNum = 79;BA.debugLine="test = 1";
_test = (int) (1);
 //BA.debugLineNum = 80;BA.debugLine="memo = Null";
_memo = (double)(BA.ObjectToNumber(anywheresoftware.b4a.keywords.Common.Null));
 break; }
case 1: {
 //BA.debugLineNum = 82;BA.debugLine="memo = chiffre";
_memo = _chiffre;
 //BA.debugLineNum = 83;BA.debugLine="Edit1.Text=\"0\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence("0"));
 //BA.debugLineNum = 84;BA.debugLine="Edit1.Tag= \"+\"";
mostCurrent._edit1.setTag((Object)("+"));
 //BA.debugLineNum = 85;BA.debugLine="test = 1";
_test = (int) (1);
 break; }
case 2: {
 //BA.debugLineNum = 87;BA.debugLine="memo = chiffre";
_memo = _chiffre;
 //BA.debugLineNum = 88;BA.debugLine="Edit1.Text=\"0\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence("0"));
 //BA.debugLineNum = 89;BA.debugLine="Edit1.Tag = \"*\"";
mostCurrent._edit1.setTag((Object)("*"));
 //BA.debugLineNum = 90;BA.debugLine="test = 1";
_test = (int) (1);
 break; }
case 3: {
 //BA.debugLineNum = 92;BA.debugLine="memo = chiffre";
_memo = _chiffre;
 //BA.debugLineNum = 93;BA.debugLine="Edit1.Text=\"0\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence("0"));
 //BA.debugLineNum = 94;BA.debugLine="Edit1.Tag = \"-\"";
mostCurrent._edit1.setTag((Object)("-"));
 //BA.debugLineNum = 95;BA.debugLine="test = 1";
_test = (int) (1);
 break; }
case 4: {
 //BA.debugLineNum = 97;BA.debugLine="memo = chiffre";
_memo = _chiffre;
 //BA.debugLineNum = 98;BA.debugLine="Edit1.Text=\"0\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence("0"));
 //BA.debugLineNum = 99;BA.debugLine="Edit1.Tag = \"/\"";
mostCurrent._edit1.setTag((Object)("/"));
 //BA.debugLineNum = 100;BA.debugLine="test = 1";
_test = (int) (1);
 break; }
case 5: {
 //BA.debugLineNum = 102;BA.debugLine="If test = 0 Then";
if (_test==0) { 
 //BA.debugLineNum = 103;BA.debugLine="Return";
if (true) return "";
 }else {
 //BA.debugLineNum = 105;BA.debugLine="Select Edit1.Tag";
switch (BA.switchObjectToInt(mostCurrent._edit1.getTag(),(Object)("c"),(Object)("+"),(Object)("-"),(Object)("*"),(Object)("/"))) {
case 0: {
 //BA.debugLineNum = 107;BA.debugLine="Edit1.Text = Null";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 108;BA.debugLine="affiche = Null";
mostCurrent._affiche = BA.ObjectToString(anywheresoftware.b4a.keywords.Common.Null);
 break; }
case 1: {
 //BA.debugLineNum = 110;BA.debugLine="Edit1.Text = memo + chiffre";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(_memo+_chiffre));
 break; }
case 2: {
 //BA.debugLineNum = 112;BA.debugLine="Edit1.Text = memo - chiffre";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(_memo-_chiffre));
 break; }
case 3: {
 //BA.debugLineNum = 114;BA.debugLine="Edit1.Text = memo * chiffre";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(_memo*_chiffre));
 break; }
case 4: {
 //BA.debugLineNum = 116;BA.debugLine="Edit1.Text = memo / chiffre";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(_memo/(double)_chiffre));
 break; }
}
;
 };
 break; }
case 6: {
 //BA.debugLineNum = 120;BA.debugLine="chiffreRef =  chiffre";
_chiffreref = (int) (_chiffre);
 //BA.debugLineNum = 121;BA.debugLine="If chiffreRef = chiffre Then";
if (_chiffreref==_chiffre) { 
 //BA.debugLineNum = 122;BA.debugLine="Edit1.Text = virtual.Text";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(_virtual.getText()));
 }else {
 //BA.debugLineNum = 124;BA.debugLine="Edit1.Text =affiche &  virtual.Text";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(mostCurrent._affiche+_virtual.getText()));
 };
 break; }
default: {
 //BA.debugLineNum = 127;BA.debugLine="Edit1.Text = chiffre & virtual.Text";
mostCurrent._edit1.setText(BA.ObjectToCharSequence(BA.NumberToString(_chiffre)+_virtual.getText()));
 break; }
}
;
 //BA.debugLineNum = 129;BA.debugLine="End Sub";
return "";
}
public static String  _action_longclick() throws Exception{
anywheresoftware.b4a.objects.ButtonWrapper _virtual = null;
 //BA.debugLineNum = 130;BA.debugLine="Sub action_LongClick";
 //BA.debugLineNum = 131;BA.debugLine="Dim virtual As Button";
_virtual = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 132;BA.debugLine="virtual = Sender";
_virtual.setObject((android.widget.Button)(anywheresoftware.b4a.keywords.Common.Sender(mostCurrent.activityBA)));
 //BA.debugLineNum = 133;BA.debugLine="Select virtual.Tag";
switch (BA.switchObjectToInt(_virtual.getTag(),(Object)("1"))) {
case 0: {
 //BA.debugLineNum = 135;BA.debugLine="Edit1.Text=\"0\"";
mostCurrent._edit1.setText(BA.ObjectToCharSequence("0"));
 break; }
}
;
 //BA.debugLineNum = 137;BA.debugLine="End Sub";
return "";
}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 53;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 55;BA.debugLine="Activity.LoadLayout(\"Layout1\")";
mostCurrent._activity.LoadLayout("Layout1",mostCurrent.activityBA);
 //BA.debugLineNum = 57;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 63;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 65;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 59;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 61;BA.debugLine="End Sub";
return "";
}
public static String  _beffacer_click() throws Exception{
 //BA.debugLineNum = 139;BA.debugLine="Sub beffacer_Click";
 //BA.debugLineNum = 140;BA.debugLine="affiche = Null";
mostCurrent._affiche = BA.ObjectToString(anywheresoftware.b4a.keywords.Common.Null);
 //BA.debugLineNum = 141;BA.debugLine="Edit1=Null";
mostCurrent._edit1.setObject((android.widget.TextView)(anywheresoftware.b4a.keywords.Common.Null));
 //BA.debugLineNum = 142;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 22;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 26;BA.debugLine="Private b1 As Button";
mostCurrent._b1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Private b4 As Button";
mostCurrent._b4 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 28;BA.debugLine="Private b7 As Button";
mostCurrent._b7 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Private bpoint As Button";
mostCurrent._bpoint = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 30;BA.debugLine="Private b2 As Button";
mostCurrent._b2 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 31;BA.debugLine="Private b5 As Button";
mostCurrent._b5 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 32;BA.debugLine="Private b8 As Button";
mostCurrent._b8 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 33;BA.debugLine="Private b0 As Button";
mostCurrent._b0 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 34;BA.debugLine="Private b3 As Button";
mostCurrent._b3 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 35;BA.debugLine="Private b6 As Button";
mostCurrent._b6 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 36;BA.debugLine="Private b9 As Button";
mostCurrent._b9 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 37;BA.debugLine="Private bequal As Button";
mostCurrent._bequal = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 38;BA.debugLine="Private bplus As Button";
mostCurrent._bplus = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 39;BA.debugLine="Private bdivision As Button";
mostCurrent._bdivision = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Private bmul As Button";
mostCurrent._bmul = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Private bmoins As Button";
mostCurrent._bmoins = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 42;BA.debugLine="Private beffacer As Button";
mostCurrent._beffacer = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 43;BA.debugLine="Private faco As Button";
mostCurrent._faco = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 45;BA.debugLine="Private affiche As String";
mostCurrent._affiche = "";
 //BA.debugLineNum = 46;BA.debugLine="Private Edit1 As Label";
mostCurrent._edit1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Private chiffre As Double";
_chiffre = 0;
 //BA.debugLineNum = 48;BA.debugLine="Private chiffreRef As Int";
_chiffreref = 0;
 //BA.debugLineNum = 49;BA.debugLine="Private memo As Double";
_memo = 0;
 //BA.debugLineNum = 50;BA.debugLine="Private test As Int";
_test = 0;
 //BA.debugLineNum = 51;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
starter._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 15;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 20;BA.debugLine="End Sub";
return "";
}
}
