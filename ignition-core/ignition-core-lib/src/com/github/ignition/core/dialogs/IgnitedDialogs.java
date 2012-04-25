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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
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

public class IgnitedDialogs {

    /**
     * Creates a new ProgressDialog
     * 
     * @param activity
     * @param progressDialogTitleId
     *            The resource id for the title. If this is less than or equal to 0, a default title
     *            is used.
     * @param progressDialogMsgId
     *            The resource id for the message. If this is less than or equal to 0, a default
     *            message is used.
     * @return The new dialog
     */
    public static ProgressDialog newProgressDialog(final Activity activity,
            int progressDialogTitleId, int progressDialogMsgId) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        if (progressDialogTitleId <= 0) {
            progressDialogTitleId = R.string.ign_progress_dialog_title;
        }
        progressDialog.setTitle(progressDialogTitleId);
        if (progressDialogMsgId <= 0) {
            progressDialogMsgId = R.string.ign_progress_dialog_msg;
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
        return progressDialog;
    }

    /**
     * Creates a new ProgressDialog with the default dialog title and message.
     * 
     * @param activity
     * @return
     */
    public static ProgressDialog newProgressDialog(final Activity activity) {
        return newProgressDialog(activity, -1, -1);
    }

    /**
     * Builds a new Yes/No AlertDialog
     * 
     * @param context
     * @param title
     * @param message
     * @param positiveButtonMessage
     * @param negativeButtonMessage
     * @param iconId
     * @param listener
     * @return
     */
    public static AlertDialog.Builder newYesNoDialog(final Context context, String title,
            String message, String positiveButtonMessage, String negativeButtonMessage, int iconId,
            OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setPositiveButton(positiveButtonMessage, listener);
        builder.setNegativeButton(negativeButtonMessage, listener);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(iconId);

        return builder;
    }

    public static AlertDialog.Builder newYesNoDialog(final Context context, String title,
            String message, int iconId, OnClickListener listener) {
        return newYesNoDialog(context, title, message, context.getString(android.R.string.yes),
                context.getString(android.R.string.no), iconId, listener);
    }

    public static AlertDialog.Builder newYesNoDialog(final Context context, int titleId,
            int messageId, int iconId, OnClickListener listener) {
        return newYesNoDialog(context, context.getString(titleId), context.getString(messageId),
                context.getString(android.R.string.yes), context.getString(android.R.string.no),
                iconId, listener);
    }

    public static AlertDialog.Builder newYesNoDialog(final Context context, int titleId,
            int messageId, int positiveButtonMessageId, int negativeButtonMessageId, int iconId,
            OnClickListener listener) {
        return newYesNoDialog(context, context.getString(titleId), context.getString(messageId),
                context.getString(positiveButtonMessageId),
                context.getString(negativeButtonMessageId), iconId, listener);
    }

    /**
     * Builds a new AlertDialog to display a simple message
     * 
     * @param context
     * @param title
     * @param message
     * @param iconId
     * @return
     */
    public static AlertDialog.Builder newMessageDialog(final Context context, String title,
            String message, int iconId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(android.R.string.ok), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(iconId);

        return builder;
    }

    public static AlertDialog.Builder newMessageDialog(final Context context, int titleId,
            int messageId, int iconId) {
        return newMessageDialog(context, context.getString(titleId), context.getString(messageId),
                iconId);
    }

    public static AlertDialog.Builder newErrorDialog(final Activity activity, String title,
            Exception error) {
        String screenMessage = "";
        if (error instanceof ResourceMessageException) {
            screenMessage = activity.getString(((ResourceMessageException) error)
                    .getClientMessageResourceId());
        } else {
            screenMessage = error.getLocalizedMessage();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
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

    public static AlertDialog.Builder newErrorDialog(final Activity activity, int titleId,
            Exception error) {
        return newErrorDialog(activity, activity.getString(titleId), error);
    }

    /**
     * Displays a error dialog with an exception's message as its body. Also displays a Send Email
     * button to send the exception to the developer, if an appropriate Intent handler is available
     * (otherwise it will behave exactly like {@link #newErrorDialog(Activity, String, Exception)}.
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
     * @param title
     * @param error
     * @return
     */
    public static AlertDialog.Builder newErrorHandlerDialog(final Activity activity, String title,
            String emailAddress, Exception error) {

        AlertDialog.Builder builder = newErrorDialog(activity, title, error);

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

        return builder;
    }

    public static AlertDialog.Builder newErrorHandlerDialog(final Activity activity, int titleId,
            String emailAddress, Exception error) {
        return newErrorHandlerDialog(activity, activity.getString(titleId), emailAddress, error);
    }

    /**
     * Creates a AlertDialog that shows a list of elements. The listener's onClick method gets
     * called when the user taps a list item.
     * 
     * @param <T>
     *            The type of each element
     * @param context
     * @param title
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
    public static <T> AlertDialog.Builder newListDialog(final Activity context, String title,
            final List<T> elements, final DialogClickListener<T> listener,
            final boolean closeOnSelect) {
        return newListDialog(context, title, elements, listener, closeOnSelect, 0);
    }

    public static <T> AlertDialog.Builder newListDialog(final Activity context, String title,
            final List<T> elements, final DialogClickListener<T> listener,
            final boolean closeOnSelect, int selectedItem) {
        final int entriesSize = elements.size();
        String[] entries = new String[entriesSize];
        for (int i = 0; i < entriesSize; i++) {
            entries[i] = elements.get(i).toString();
        }

        Builder builder = new AlertDialog.Builder(context);
        if (title != null) {
            builder.setTitle(title);
        }
        builder.setSingleChoiceItems(entries, selectedItem, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (closeOnSelect) {
                    dialog.dismiss();
                }
                listener.onClick(which, elements.get(which));
            }
        });

        return builder;
    }
}
