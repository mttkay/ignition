/* Copyright (c) 2009-2011 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.ignition.core.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;
import com.github.ignition.core.R;
import com.github.ignition.core.exceptions.ResourceMessageException;
import com.github.ignition.support.IgnitedDiagnostics;
import com.github.ignition.support.IgnitedIntents;

import java.util.List;

public class IgnitedDialogs {

    private static final String PROGRESS_DIALOG_TITLE_RESOURCE = "droidfu_progress_dialog_title";

    private static final String PROGRESS_DIALOG_MESSAGE_RESOURCE = "droidfu_progress_dialog_message";

    public static final String ERROR_DIALOG_TITLE_RESOURCE = "droidfu_error_dialog_title";

    /**
     * Creates a new ProgressDialog
     * 
     * @param activity
     * @param progressDialogTitleId
     *            The resource id for the title. If this is less than or equal to 0, a default title
     *            is used.
     * @param progressDialogMsgId
     *            The resource id for the message.
     * @return The new dialog
     */
    public static ProgressDialog createProgressDialog(final Activity activity,
            int progressDialogTitleId, int progressDialogMsgId) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        if (progressDialogTitleId <= 0) {
            progressDialogTitleId = activity.getResources().getIdentifier(
                    PROGRESS_DIALOG_TITLE_RESOURCE, "string", activity.getPackageName());
        }
        progressDialog.setTitle(progressDialogTitleId);
        if (progressDialogMsgId <= 0) {
            progressDialogMsgId = activity.getResources().getIdentifier(
                    PROGRESS_DIALOG_MESSAGE_RESOURCE, "string", activity.getPackageName());
        }
        progressDialog.setMessage(activity.getString(progressDialogMsgId));
        progressDialog.setIndeterminate(true);
        progressDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                activity.onKeyDown(keyCode, event);
                return false;
            }
        });
        // progressDialog.setInverseBackgroundForced(true);
        return progressDialog;
    }

    /**
     * Creates a new Yes/No AlertDialog
     * 
     * @param context
     * @param dialogTitle
     * @param screenMessage
     * @param iconResourceId
     * @param listener
     * @return
     */
    public static AlertDialog newYesNoDialog(final Context context, String dialogTitle,
            String screenMessage, int iconResourceId, OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.yes, listener);
        builder.setNegativeButton(android.R.string.no, listener);

        builder.setTitle(dialogTitle);
        builder.setMessage(screenMessage);
        builder.setIcon(iconResourceId);

        return builder.create();
    }

    /**
     * Creates a new AlertDialog to display a simple message
     * 
     * @param context
     * @param dialogTitle
     * @param screenMessage
     * @param iconResourceId
     * @return
     */
    public static AlertDialog newMessageDialog(final Context context, String dialogTitle,
            String screenMessage, int iconResourceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setTitle(dialogTitle);
        builder.setMessage(screenMessage);
        builder.setIcon(iconResourceId);

        return builder.create();
    }

    public static AlertDialog newErrorDialog(final Activity activity, String dialogTitle,
            Exception error) {
        return createErrorDialog(activity, error, dialogTitle).create();
    }

    /**
     * Displays a error dialog with an exception as its body. Also displays a Send Email button to
     * send the exception to the developer, if an appropriate Intent handler is available (otherwise
     * it will behave exactly like {@link #newErrorDialog(Activity, String, Exception)}.
     * 
     * <p>
     * Email subject and button label will have default values, but you can override them by
     * defining the following strings resources:
     * 
     * <ul>
     * <li>ign_error_report_email_subject - The subject of the email.</li>
     * <li>ign_dialog_button_send_error_report - The text on the Send Email button.</li>
     * </ul>
     * </p>
     * 
     * @param activity
     * @param dialogTitle
     * @param error
     * @return
     */
    public static AlertDialog newErrorHandlerDialog(final Activity activity, String dialogTitle,
            String emailAddress, Exception error) {

        AlertDialog.Builder builder = createErrorDialog(activity, error, dialogTitle);

        if (IgnitedIntents.isIntentAvailable(activity, Intent.ACTION_SEND,
                IgnitedIntents.MIME_TYPE_EMAIL)) {
            String buttonText = activity.getString(R.string.ign_error_report_send_button);
            String bugReportEmailSubject = activity.getString(
                    R.string.ign_error_report_email_subject, error.getClass().getName());
            final String diagnosis = IgnitedDiagnostics.createDiagnosis(activity, error);
            final Intent intent = IgnitedIntents.newEmailIntent(activity, emailAddress,
                    bugReportEmailSubject, diagnosis);
            builder.setNegativeButton(buttonText, new OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    activity.startActivity(intent);
                }
            });
        }

        return builder.create();
    }

    /**
     * Creates a AlertDialog that shows a list of elements. The listener's onClick method gets
     * called when the user taps a list item.
     * 
     * @param <T>
     *            The type of each element
     * @param context
     * @param dialogTitle
     *            the title or null to disable the title
     * @param elements
     *            List of elements to be displayed. Each elements toString() method will be called.
     * @param listener
     *            The listener to handle the onClick events.
     * @param closeOnSelect
     *            If true the dialog closes as soon as one list item is selected, otherwise multiple
     *            onClick events may be sent.
     * @return The new dialog.
     */
    public static <T> Dialog newListDialog(final Activity context, String dialogTitle,
            final List<T> elements, final DialogClickListener<T> listener,
            final boolean closeOnSelect) {
        return newListDialog(context, dialogTitle, elements, listener, closeOnSelect, 0);
    }

    public static <T> Dialog newListDialog(final Activity context, String dialogTitle,
            final List<T> elements, final DialogClickListener<T> listener,
            final boolean closeOnSelect, int selectedItem) {
        final int entriesSize = elements.size();
        String[] entries = new String[entriesSize];
        for (int i = 0; i < entriesSize; i++) {
            entries[i] = elements.get(i).toString();
        }

        Builder builder = new AlertDialog.Builder(context);
        if (dialogTitle != null) {
            builder.setTitle(dialogTitle);
        }
        builder.setSingleChoiceItems(entries, selectedItem, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (closeOnSelect)
                    dialog.dismiss();
                listener.onClick(which, elements.get(which));
            }
        });

        return builder.create();
    }

    private static AlertDialog.Builder createErrorDialog(final Activity activity, Exception error,
            String dialogTitle) {
        String screenMessage;
        if (error instanceof ResourceMessageException) {
            screenMessage = activity.getString(((ResourceMessageException) error)
                    .getClientMessageResourceId());
        } else {
            screenMessage = error.getLocalizedMessage();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dialogTitle);
        builder.setMessage(screenMessage);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(false);
        builder.setPositiveButton(activity.getString(android.R.string.ok), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder;
    }
}
