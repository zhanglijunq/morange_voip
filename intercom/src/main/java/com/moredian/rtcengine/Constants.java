package com.moredian.rtcengine;

public class Constants {
	public static final int MSG_ERR_NONE = 0;
	public static final int MSG_ERR_INVALID_PARAM = 1;
	public static final int MSG_ERR_FAIL = 2;
	public static final int MSG_ERR_TIMEOUT = 3;
	public static final int MSG_ERR_CANCEL = 4;
	public static final int MSG_ERR_PARSE_JSON_FAIL = 5;
	public static final int MSG_ERR_NO_NET_CONNECTION = 6;
	public static final int MSG_ERR_WRONG_PASSWORD = 7;// login: wrong password
	public static final int MSG_ERR_NOT_REGISTER = 8;// login: not register
	public static final int MSG_ERR_NOT_LOGIN = 9;
	public static final int MSG_ERR_UNSUPPORT = 10;
	
	//���»�ȡ���������ݵĲ���������������List<Object>
	// input:null
	// result:(Activities)allActivities + (Activities)historicalActivities
	public static final int MSG_HTTP_EJ_INDEX_URL = 0x101000;
	// input: null
	// result: (UserInfo)userInfo + (Activities)allActivities +
	// (Activities)historicalActivities
	public static final int MSG_HTTP_EJ_GET_HISTORICAL_URL = MSG_HTTP_EJ_INDEX_URL + 1;
	public static final int MSG_HTTP_EJ_VIEW_DETAIL_URL = MSG_HTTP_EJ_INDEX_URL + 2;//(int)activity_ID + (String)is_new
	public static final int MSG_HTTP_EJ_CREATE_URL = MSG_HTTP_EJ_INDEX_URL + 3;// (SingleActivity)singleActivity
	public static final int MSG_HTTP_EJ_EDIT_URL = MSG_HTTP_EJ_INDEX_URL + 4;// (SingleActivity)singleActivity
	public static final int MSG_HTTP_EJ_CANCEL_URL = MSG_HTTP_EJ_INDEX_URL + 5;// (int)activity_ID
	public static final int MSG_HTTP_EJ_REOPEN_URL = MSG_HTTP_EJ_INDEX_URL + 6;// (int)activity_ID
	public static final int MSG_HTTP_EJ_REPLY_URL = MSG_HTTP_EJ_INDEX_URL + 7;// input:
																				// (int)activity_ID
																				// +
																				// (List<DateTime>)
																				// dateTimeList
																				// +
																				// (int)current_user_num
																				// results:(SingleActivity)activity
	public static final int MSG_HTTP_EJ_ABORT_URL = MSG_HTTP_EJ_INDEX_URL + 8;// (int)activity_ID
																				// +
																				// (int)datetime_ID
	public static final int MSG_HTTP_EJ_PARTICIPATE_URL = MSG_HTTP_EJ_INDEX_URL + 9;// (int)activity_ID
																					// +
																					// (int)datetime_ID
																					// +
																					// (int)currentUserNumber
	public static final int MSG_HTTP_EJ_CLOSE_POLL_URL = MSG_HTTP_EJ_INDEX_URL + 10;// (int)activity_ID
																					// +
																					// (int)datetime_ID
	public static final int MSG_HTTP_EJ_ADD_COMMENT_URL = MSG_HTTP_EJ_INDEX_URL + 11;// input:
																						// (int)activity_ID
																						// +
																						// (String)create_datetime
																						// +
																						// (String)content
																						// +
																						// (int)comment_ID
																						// +
																						// (String)CommentRefreshType.XXX
																						// +
																						// (List<Integer>)receiver_ID
																						// +
																						// (String)ContentType.XXX
																						// result:(List<Comment>)comments
																						// +
																						// (String:
																						// YesNoString.XXX)comment_gap
																						// +
																						// (String)CommentRefreshType.XXX
	public static final int MSG_HTTP_EJ_GET_ACTIVITY_COMMENTS_URL = MSG_HTTP_EJ_INDEX_URL + 12;// input:(int)activity_ID
																								// +
																								// (int)comment_ID
																								// +
																								// (String)CommentRefreshType.XXX
																								// result:(List<Comment>)comments
																								// +
																								// (String:
																								// YesNoString.XXX)comment_gap
																								// +
																								// (String)CommentRefreshType.XXX
	public static final int MSG_HTTP_EJ_GET_ACTIVITY_LOGS_URL = MSG_HTTP_EJ_INDEX_URL + 13;
	public static final int MSG_HTTP_EJ_RESET_COMMENTS_URL = MSG_HTTP_EJ_INDEX_URL + 14;// (int)activity_ID
	public static final int MSG_HTTP_EJ_RESET_ACTIVITY_URL = MSG_HTTP_EJ_INDEX_URL + 15;// (int)activity_ID
	// input:null
	// result:null
	public static final int MSG_HTTP_EJ_REGISTER_DEVICE_URL = MSG_HTTP_EJ_INDEX_URL + 16;
	// input:null
	// result:null
	public static final int MSG_HTTP_EJ_UNREGISTER_DEVICE_URL = MSG_HTTP_EJ_INDEX_URL + 17;
	// input:(String)appVersion + (String)platform + (String)model +
	// (String)systemVersion
	// result:CheckVersionResult
	public static final int MSG_HTTP_EJ_CHECK_VERSION_URL = MSG_HTTP_EJ_INDEX_URL + 18;
	// input:(List<Friend>) friends
	// result:(List<EJFriend>) ejFriends
	public static final int MSG_HTTP_EJ_GET_EJFRIENDS_URL = MSG_HTTP_EJ_INDEX_URL + 19;
	// input:(String)phoneNumber + (String)password
	// result:(UserInfo)userInfo + (Activities)allActivities +
	// (Activities)historicalActivites
	public static final int MSG_HTTP_EJ_USER_LOGIN_URL = MSG_HTTP_EJ_INDEX_URL + 20;
	// input:null
	// result:null
	public static final int MSG_HTTP_EJ_USER_LOGOUT_URL = MSG_HTTP_EJ_INDEX_URL + 21;
	// input:(String)phone_num
	// result:(String)msg
	public static final int MSG_HTTP_EJ_USER_REGISTER_PHONE_URL = MSG_HTTP_EJ_INDEX_URL + 22;
	// input:(String)phone_num + (String)security_token
	// result: (String)msg
	public static final int MSG_HTTP_EJ_USER_VERIFY_SECURITY_TOKEN_URL = MSG_HTTP_EJ_INDEX_URL + 23;
	// intput:(String)name + (String)passWord + (String)profile_image_name +
	// (String)phone_num + (String)profile_image_path
	// result:(UserInfo)userInfo + (Activities)allActivities +
	// (Activities)historicalActivities
	public static final int MSG_HTTP_EJ_USER_FINISH_REGISTER_URL = MSG_HTTP_EJ_INDEX_URL + 24;
	// input:(String)phoneNum + (String)name + (String)profileImageName
	// result:(UserInfo)userInfo
	public static final int MSG_HTTP_EJ_SAVE_USER_SETTING_URL = MSG_HTTP_EJ_INDEX_URL + 25;
	public static final int MSG_HTTP_EJ_UPLOAD_IMAGE_URL = MSG_HTTP_EJ_INDEX_URL + 26;// unused
	// input:
	// result:
	public static final int MSG_HTTP_EJ_GET_REGISTER_TOKEN_URL = MSG_HTTP_EJ_INDEX_URL + 27;// internal
	// input: (String)phoneNum
	// result:(String)message
	public static final int MSG_HTTP_EJ_USER_PREPARE_RESET_PASSWORD_URL = MSG_HTTP_EJ_INDEX_URL + 28;
	// input: (String)phoneNum + (String)passWord + (String)securityToken
	// result:(UserInfo)userInfo + (Activities)allActivities
	public static final int MSG_HTTP_EJ_USER_RESET_PASSWORD_URL = MSG_HTTP_EJ_INDEX_URL + 29;
	// input:(String)systemVersion + (String)appVersion + (String)userMail +
	// (String)userFeedback + (String)model
	// result:null
	public static final int MSG_HTTP_EJ_SAVE_USER_FEEDBACK_URL = MSG_HTTP_EJ_INDEX_URL + 30;
	// input:(int)activityID
	// result:(SingleActivity)activity
	public static final int MSG_HTTP_EJ_CANCEL_CONFIRMED_DATETIME_URL = MSG_HTTP_EJ_INDEX_URL + 31;
	// input:(String)phone_num + (String: YesNo.XXX)isResetPassword
	// result:(String)msg
	public static final int MSG_HTTP_EJ_GET_SECURITY_TOKEN_URL = MSG_HTTP_EJ_INDEX_URL + 32;

	//����ΪEngine��������APP��msg
	public static final int MSG_WEB_ACTIVITY_UPDATED = 0x201000;
}
