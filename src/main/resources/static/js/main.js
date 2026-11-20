const availableRoles = [
  { roleName: 'ROLE_USER' },
  { roleName: 'ROLE_ADMIN' }
];

function formatRoles(roles) {
  return (roles || []).map(role => role.roleName.replace('ROLE_', '')).join(', ');
}

function createCell(value) {
  const cell = document.createElement('td');
  cell.textContent = value ?? '';
  return cell;
}

async function loadUsers() {
  const response = await fetch('/users/');
  if (!response.ok) {
    throw new Error(`Could not load users: ${response.status}`);
  }

  const users = await response.json();
  const tableBody = document.querySelector('#usersTable tbody');
  tableBody.replaceChildren();

  users.forEach(user => {
    const row = document.createElement('tr');
    [user.id, user.firstName, user.lastName, user.age, user.email, formatRoles(user.roles)]
      .forEach(value => row.append(createCell(value)));
    row.append(createActionCell('Edit', 'btn-info', 'edit', user.id));
    row.append(createActionCell('Delete', 'btn-danger', 'delete', user.id));
    tableBody.append(row);
  });
}

function createActionCell(label, buttonClass, action, userId) {
  const cell = document.createElement('td');
  const button = document.createElement('button');
  button.className = `btn ${buttonClass}`;
  button.type = 'button';
  button.textContent = label;
  button.dataset.action = action;
  button.dataset.id = userId;
  cell.append(button);
  return cell;
}

function getSelectedRoles(select) {
  return [...select.selectedOptions].map(option => ({ roleName: option.value }));
}

function appendRoleSelect(container, selectedRoleNames = [], disabled = false) {
  const label = document.createElement('label');
  label.className = 'form-group d-block';
  label.textContent = 'Role';

  const select = document.createElement('select');
  select.className = 'form-control';
  select.name = 'roles';
  select.multiple = true;
  select.disabled = disabled;

  availableRoles.forEach(role => {
    const option = document.createElement('option');
    option.value = role.roleName;
    option.textContent = role.roleName.replace('ROLE_', '');
    option.selected = selectedRoleNames.includes(role.roleName);
    select.append(option);
  });

  label.append(select);
  container.append(label);
  return select;
}

function appendInput(container, labelText, name, value = '', disabled = false, type = 'text') {
  const label = document.createElement('label');
  label.className = 'form-group d-block';
  label.textContent = labelText;

  const input = document.createElement('input');
  input.className = 'form-control';
  input.name = name;
  input.value = value ?? '';
  input.disabled = disabled;
  input.type = type;
  label.append(input);
  container.append(label);
  return input;
}

function populateUserForm(container, user, disabled) {
  container.replaceChildren();
  container.closest('form').dataset.currentPassword = user.password || '';
  appendInput(container, 'ID', 'id', user.id, false, 'hidden');
  appendInput(container, 'First name', 'firstName', user.firstName, disabled);
  appendInput(container, 'Last name', 'lastName', user.lastName, disabled);
  appendInput(container, 'Age', 'age', user.age, disabled, 'number');
  appendInput(container, 'Email', 'email', user.email, disabled, 'email');
  const passwordInput = appendInput(container, 'Password', 'password', '', disabled, 'password');
  passwordInput.autocomplete = disabled ? 'off' : 'new-password';
  if (!disabled) {
    passwordInput.placeholder = 'Leave blank to keep current password';
  }
  appendRoleSelect(container, (user.roles || []).map(role => role.roleName), disabled);
}

function showModal(selector) {
  window.jQuery(selector).modal('show');
}

function hideModal(selector) {
  window.jQuery(selector).modal('hide');
}

function activateTab(trigger) {
  const targetPanel = document.getElementById(trigger.dataset.tabTarget);
  if (!targetPanel) {
    return;
  }

  const panelGroup = targetPanel.parentElement;
  [...panelGroup.children]
    .filter(panel => panel.classList.contains('tab-pane'))
    .forEach(panel => panel.classList.toggle('active', panel === targetPanel));

  const tabList = trigger.closest('[data-tab-list]');
  if (tabList) {
    tabList.querySelectorAll(':scope > [data-tab-target], :scope > li > [data-tab-target]').forEach(tab => {
      tab.classList.toggle('active', tab === trigger);
    });
  }
}

function bindTabNavigation() {
  document.addEventListener('click', event => {
    const trigger = event.target.closest('[data-tab-target]');
    if (!trigger) {
      return;
    }

    event.preventDefault();
    activateTab(trigger);
  });
}

function formToUser(form) {
  const formData = new FormData(form);
  const user = Object.fromEntries(formData.entries());
  user.age = Number(user.age);
  if (!user.password && form.id === 'editForm') {
    user.password = form.dataset.currentPassword || '';
  }
  user.roles = getSelectedRoles(form.querySelector('select[name="roles"]'));
  return user;
}

async function sendUserRequest(url, method, user) {
  const response = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user)
  });
  if (!response.ok) {
    const responseBody = await response.text();
    let message = responseBody;
    try {
      message = JSON.parse(responseBody).message || responseBody;
    } catch {
      // Keep the plain response text when the server did not return JSON.
    }
    throw new Error(message || `Request failed: ${response.status}`);
  }
}

function bindCreateForm() {
  const form = document.querySelector('#createForm');
  const errorBox = document.querySelector('#createError');
  form.addEventListener('submit', async event => {
    event.preventDefault();
    errorBox.classList.add('d-none');
    errorBox.textContent = '';
    form.querySelectorAll('.is-invalid').forEach(field => field.classList.remove('is-invalid'));

    const requiredFields = [...form.querySelectorAll('[required]')];
    let isValid = true;
    requiredFields.forEach(field => {
      const fieldIsValid = field.type === 'select-multiple'
        ? field.selectedOptions.length > 0
        : field.checkValidity();
      field.classList.toggle('is-invalid', !fieldIsValid);
      isValid = isValid && fieldIsValid;
    });
    if (!isValid) {
      showFormError(errorBox, 'Проверьте обязательные поля и выберите хотя бы одну роль. Возраст должен быть от 18 до 150.');
      return;
    }

    try {
      await sendUserRequest('/users/', 'POST', formToUser(form));
      form.reset();
      await loadUsers();
      document.querySelector('[data-tab-target="users-table"]').click();
    } catch (error) {
      console.error('Error creating user:', error);
      showFormError(errorBox, getReadableError(error));
    }
  });
}

function showFormError(container, message) {
  container.textContent = message;
  container.classList.remove('d-none');
}

function getReadableError(error) {
  const message = error?.message || 'Не удалось добавить пользователя.';
  try {
    const details = JSON.parse(message);
    return details.message || details.error || details.errors || message;
  } catch {
    return message || 'Не удалось добавить пользователя.';
  }
}

function bindUserActions() {
  document.querySelector('#usersTable tbody').addEventListener('click', async event => {
    const button = event.target.closest('button[data-action]');
    if (!button) {
      return;
    }

    try {
      const response = await fetch(`/users/${button.dataset.id}`);
      if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
      }

      const user = await response.json();
      if (button.dataset.action === 'edit') {
        populateUserForm(document.querySelector('#editFields'), user, false);
        showModal('#editModal');
      } else {
        populateUserForm(document.querySelector('#deleteFields'), user, true);
        showModal('#deleteModal');
      }
    } catch (error) {
      console.error(error);
      alert('Could not open user details');
    }
  });
}

function bindEditForm() {
  document.querySelector('#editForm').addEventListener('submit', async event => {
    event.preventDefault();
    try {
      const user = formToUser(event.currentTarget);
      await sendUserRequest(`/users/${user.id}`, 'PATCH', user);
      hideModal('#editModal');
      await loadUsers();
    } catch (error) {
      console.error('Error updating user:', error);
      alert(getReadableError(error));
    }
  });
}

function bindDeleteForm() {
  document.querySelector('#deleteForm').addEventListener('submit', async event => {
    event.preventDefault();
    const userId = document.querySelector('#deleteFields input[name="id"]').value;

    try {
      const response = await fetch(`/users/${userId}`, { method: 'DELETE' });
      if (!response.ok) {
        throw new Error(await response.text());
      }
      hideModal('#deleteModal');
      await loadUsers();
    } catch (error) {
      console.error('Error deleting user:', error);
      alert(getReadableError(error));
    }
  });
}

document.addEventListener('DOMContentLoaded', () => {
  bindTabNavigation();
  loadUsers().catch(console.error);
  bindCreateForm();
  bindUserActions();
  bindEditForm();
  bindDeleteForm();
});
