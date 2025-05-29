const API_BASE_URL = 'http://localhost:9090/api/contacts'; // adjust your API base URL

// Cached DOM elements
const contactListView = document.getElementById('contact-list-view');
const contactFormView = document.getElementById('contact-form-view');
const contactsUl = document.getElementById('contacts-ul');
const addContactBtn = document.getElementById('add-contact-btn');
const cancelBtn = document.getElementById('cancel-btn');
const formTitle = document.getElementById('form-title');
const contactForm = document.getElementById('contact-form');
const saveContactBtn = document.getElementById('save-contact-btn');

let editingContactId = null;

// Initialize Materialize labels
document.addEventListener('DOMContentLoaded', () => {
  M.updateTextFields();
});

// Show list view, hide form
function showListView() {
  contactFormView.style.display = 'none';
  contactListView.style.display = 'block';
  editingContactId = null;
  contactForm.reset();
  M.updateTextFields();
}

// Show form view for add or update
function showFormView(edit = false, contact = null) {
  contactListView.style.display = 'none';
  contactFormView.style.display = 'block';

  if (edit && contact) {
    formTitle.textContent = 'Update Contact';
    document.getElementById('name').value = contact.name;
    document.getElementById('email').value = contact.email;
    document.getElementById('phone').value = contact.phone;
    editingContactId = contact.id;
  } else {
    formTitle.textContent = 'Add Contact';
    contactForm.reset();
    editingContactId = null;
  }

  M.updateTextFields();
}

// Fetch contacts and populate list
function fetchContacts() {
  fetch(API_BASE_URL)
    .then(res => res.json())
    .then(data => {
      contactsUl.innerHTML = '';
      data.forEach(contact => {
        const li = document.createElement('li');
        li.className = 'collection-item';

        li.innerHTML = `
          <span><strong>${contact.name}</strong> - ${contact.email} - ${contact.phone}</span>
          <a href="#!" class="secondary-content">
            <i class="material-icons edit-btn" style="cursor:pointer" data-id="${contact.id}">edit</i>
            <i class="material-icons delete-btn" style="cursor:pointer; margin-left: 10px;" data-id="${contact.id}">delete</i>
          </a>
        `;
        contactsUl.appendChild(li);
      });

      // Attach event listeners for edit and delete buttons
      document.querySelectorAll('.edit-btn').forEach(btn => {
        btn.addEventListener('click', e => {
          const id = e.target.dataset.id;
          editContact(id);
        });
      });

      document.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', e => {
          const id = e.target.dataset.id;
          deleteContact(id);
        });
      });
    })
    .catch(err => {
      M.toast({html: 'Error fetching contacts'});
      console.error(err);
    });
}

// Load contact for editing
function editContact(id) {
  fetch(`${API_BASE_URL}/${id}`)
    .then(res => res.json())
    .then(contact => {
      showFormView(true, contact);
    })
    .catch(err => {
      M.toast({html: 'Error loading contact'});
      console.error(err);
    });
}

// Delete contact with confirmation
function deleteContact(id) {
  if (!confirm('Are you sure you want to delete this contact?')) return;

  fetch(`${API_BASE_URL}/${id}`, { method: 'DELETE' })
    .then(res => {
      if (res.ok) {
        M.toast({html: 'Contact deleted'});
        fetchContacts();
      } else {
        M.toast({html: 'Failed to delete contact'});
      }
    })
    .catch(err => {
      M.toast({html: 'Error deleting contact'});
      console.error(err);
    });
}

// Save contact (add or update)
contactForm.addEventListener('submit', e => {
  e.preventDefault();

  const contactData = {
    name: document.getElementById('name').value.trim(),
    email: document.getElementById('email').value.trim(),
    phone: document.getElementById('phone').value.trim(),
  };

  let method = 'POST';
  let url = API_BASE_URL;

  if (editingContactId) {
    method = 'PUT';
    url = `${API_BASE_URL}/${editingContactId}`;
  }

  fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(contactData)
  })
  .then(res => {
    if (res.ok) {
      M.toast({html: editingContactId ? 'Contact updated' : 'Contact added'});
      showListView();
      fetchContacts();
    } else {
      M.toast({html: 'Failed to save contact'});
    }
  })
  .catch(err => {
    M.toast({html: 'Error saving contact'});
    console.error(err);
  });
});

// Cancel button handler
cancelBtn.addEventListener('click', e => {
  e.preventDefault();
  showListView();
});

// Add Contact button handler
addContactBtn.addEventListener('click', e => {
  showFormView();
});

// Initial fetch of contacts on page load
fetchContacts();
