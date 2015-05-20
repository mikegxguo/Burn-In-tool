package bom.mitac.bist.burnin.rebootaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO 自動生�??�れ?�メソッド・スタ�?
		SETTING_XML_DATA	SettingXmlData = new SETTING_XML_DATA() ;


		RebootAgingCmn.GetSettingXmlData( SettingXmlData, context.getFileStreamPath( "Setting.xml" ).getPath() ) ;
		if( SettingXmlData.StartFlag )
		{
			context.startActivity( new Intent( context, AgingActivity.class ).setFlags( Intent.FLAG_ACTIVITY_NEW_TASK ) ) ;
		}
	}
}
