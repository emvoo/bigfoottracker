package uk.ac.solent.marcinwisniewski.bigfoottracker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import uk.ac.solent.marcinwisniewski.bigfoottracker.db.UserDetailsDB;

public class RegisterFragment extends Fragment {
    private EditText username, password;
    private Button login;
    private UserDetailsDB userDB;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register_fragment, container, false);
        init(view);
        return view;
    }

    /**
     * Initiate variables in this view.
     *
     * @param view
     */
    private void init(View view)
    {
        username = view.findViewById(R.id.username);
        password = view.findViewById(R.id.password);
        login = view.findViewById(R.id.login);
        login.setOnClickListener(loginClickListener);
    }

    /**
     * Set click listener
     */
    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name = getValue(username);
            String pw = getValue(password);
            userDB = ((MainActivity) getActivity()).userDB;
            // validate entered data
            if (validateUsername(name) || validatePassword(pw)) {
                // attempt to create new user
                long result = userDB.insert(-1, name, pw);
                // check if user has been created successfully
                if (result == -1) {
                    displayMessage("Error creating user.");
                    return;
                } else {
                    int id = (int) result;
                    Cursor cursor = userDB.getById(id);
                    if (cursor.moveToFirst()) {
//                        String user = cursor.getString(cursor.getColumnIndex("username"));
                        // load user data
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);

                    }
                }
            }
        }
    };

    /**
     * Helper function to retrieve string value from any EditText field.
     *
     * @param value
     * @return
     */
    private String getValue(EditText value)
    {
        return value.getText().toString().trim();
    }

    /**
     * Validate username.
     *
     * @param username
     * @return
     */
    private boolean validateUsername(String username) {
        boolean valid = true;
        if (username.isEmpty()) {
            displayMessage("Username/email cannot be empty.");
            valid = false;
        }
        if (username.length() < 5) {
            displayMessage("Username/email needs at least 5 characters.");
            valid = false;
        }
        return valid;
    }

    /**
     * Validate password.
     *
     * @param password
     * @return
     */
    private boolean validatePassword(String password) {
        boolean valid = true;
        if (password.isEmpty()) {
            displayMessage("Password cannot be empty.");
            valid = false;
        }

        if (password.length() < 8) {
            displayMessage("Password needs at least 8 characters.");
            valid = false;
        }

        return valid;
    }

    /**
     * Display error message.
     *
     * @param message
     */
    private void displayMessage(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
