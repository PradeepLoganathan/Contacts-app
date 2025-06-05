// API endpoints
const API_URL = '/contacts';

// DOM Elements
const contactsList = document.getElementById('contactsList');
const addContactForm = document.getElementById('addContactForm');

// Fetch all contacts
async function fetchContacts() {
    try {
        const response = await fetch(API_URL);
        const contacts = await response.json();
        displayContacts(contacts);
    } catch (error) {
        console.error('Error fetching contacts:', error);
        showAlert('Error loading contacts', 'danger');
    }
}

// Display contacts in the list
function displayContacts(contacts) {
    contactsList.innerHTML = '';
    contacts.forEach(contact => {
        const contactCard = createContactCard(contact);
        contactsList.appendChild(contactCard);
    });
}

// Create a contact card element
function createContactCard(contact) {
    const card = document.createElement('div');
    card.className = 'card contact-card';
    card.innerHTML = `
        <div class="card-body">
            <h5 class="card-title">${contact.name}</h5>
            <p class="card-text">
                <strong>Email:</strong> ${contact.email}<br>
                <strong>Phone:</strong> ${contact.phone}
            </p>
            <div class="actions">
                <button class="btn btn-sm btn-primary edit-btn" data-id="${contact.id}">Edit</button>
                <button class="btn btn-sm btn-danger delete-btn" data-id="${contact.id}">Delete</button>
            </div>
        </div>
    `;

    // Add event listeners
    card.querySelector('.edit-btn').addEventListener('click', () => editContact(contact));
    card.querySelector('.delete-btn').addEventListener('click', () => deleteContact(contact.id));

    return card;
}

// Add new contact
async function addContact(event) {
    event.preventDefault();

    const contact = {
        name: document.getElementById('name').value,
        email: document.getElementById('email').value,
        phone: document.getElementById('phone').value
    };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(contact)
        });

        if (response.ok) {
            showAlert('Contact added successfully', 'success');
            addContactForm.reset();
            fetchContacts();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error adding contact', 'danger');
        }
    } catch (error) {
        console.error('Error adding contact:', error);
        showAlert('Error adding contact', 'danger');
    }
}

// Edit contact
async function editContact(contact) {
    // Populate form with contact data
    document.getElementById('name').value = contact.name;
    document.getElementById('email').value = contact.email;
    document.getElementById('phone').value = contact.phone;

    // Change form submit handler
    addContactForm.onsubmit = async (event) => {
        event.preventDefault();
        
        const updatedContact = {
            name: document.getElementById('name').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value
        };

        try {
            const response = await fetch(`${API_URL}/${contact.id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(updatedContact)
            });

            if (response.ok) {
                showAlert('Contact updated successfully', 'success');
                addContactForm.reset();
                addContactForm.onsubmit = addContact; // Reset form handler
                fetchContacts();
            } else {
                const error = await response.json();
                showAlert(error.message || 'Error updating contact', 'danger');
            }
        } catch (error) {
            console.error('Error updating contact:', error);
            showAlert('Error updating contact', 'danger');
        }
    };
}

// Delete contact
async function deleteContact(id) {
    if (!confirm('Are you sure you want to delete this contact?')) {
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('Contact deleted successfully', 'success');
            fetchContacts();
        } else {
            const error = await response.json();
            showAlert(error.message || 'Error deleting contact', 'danger');
        }
    } catch (error) {
        console.error('Error deleting contact:', error);
        showAlert('Error deleting contact', 'danger');
    }
}

// Show alert message
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;
    
    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);

    // Auto-dismiss after 3 seconds
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

// Event Listeners
addContactForm.addEventListener('submit', addContact);

// Initial load
fetchContacts(); 