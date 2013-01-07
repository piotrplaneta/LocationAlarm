package edu.uj.pmd.locationalarm.database.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import edu.uj.pmd.locationalarm.R;
import edu.uj.pmd.locationalarm.database.DatabaseHandler;
import edu.uj.pmd.locationalarm.database.Destination;

/**
 * User: piotrplaneta
 * Date: 29.12.2012
 * Time: 19:59
 */
public class DeleteFavoriteDestinationTask extends AsyncTask<Destination, Void, Void> {
    private Context context;

    public DeleteFavoriteDestinationTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Destination... destinations) {
        DatabaseHandler database = new DatabaseHandler(context);

        int count = destinations.length;
        for(int i = 0; i < count; i++) {
            database.deleteFavoriteDestination(destinations[i]);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, R.string.destination_deleted, Toast.LENGTH_LONG).show();
        super.onPostExecute(aVoid);
    }
}
