package uk.ac.solent.marcinwisniewski.bigfoottracker;

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
    private EditText email, password;
    private Button register;
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
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        register = view.findViewById(R.id.register);
        register.setOnClickListener(loginClickListener);
    }

    /**
     * Set click listener
     */
    private View.OnClickListener loginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String name = getValue(email);
            String pw = getValue(password);
            userDB = ((MainActivity) getActivity()).userDB;
            // validate entered data
            if (validateUsername(name) && validatePassword(pw)) {
                // attempt to create new user
                long result = userDB.insert(name, pw);
                // check if user has been created successfully
                if (result == -1) {
                    displayMessage("Error creating user.");
                } else {
                    int id = (int) result;
                    Cursor cursor = userDB.getById(id);
                    if (cursor.moveToFirst()) {
//                        String user = cursor.getString(cursor.getColumnIndex("email"));
                        getActivity().getSupportFragmentManager().beginTransaction().remove(RegisterFragment.this).commit();
                        ((MainActivity) getActivity()).showDisplay(null);
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
     * Validate email.
     *
     * @param username
     * @return
     */
    private boolean validateUsername(String username) {
        boolean valid = true;
        if (username.isEmpty()) {
            displayMessage("Email field cannot be empty.");
            valid = false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            displayMessage("Entered data is not valid email address.");
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
