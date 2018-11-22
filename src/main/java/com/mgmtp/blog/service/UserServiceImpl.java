package com.mgmtp.blog.service;

import com.mgmtp.blog.dao.UserRepository;
import com.mgmtp.blog.model.User;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
	
	private static final int LENGTH_OF_RANDOM_PASS = 5;
	public static final int SALT_BYTES = 8;

	@Autowired
	private UserRepository userRepository;

    @Autowired
    private PasswordServiceImpl passwordService;

    @Override
	public List<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}
  
    @Override
    public List<User> findAll() {
    		return userRepository.findAll();
    }
	
	@Override
	public boolean validateUser(String username, String password) {
		List<User> users = (List<User>) findByUsername(username);
		if(users == null || users.isEmpty()) {
			return false;
		}
		
		// Encode password before validating
	
		password = passwordService.pbkdf2(password, users.get(0).getSalt());
		
		return password.equals(users.get(0).getPassword());
		
	}
	
	@Override
	public boolean resetAllPassword() {
		try {
			int numberOfUsers = findAll().size();
			List<String> passwords = passwordService.getInitialPassword(numberOfUsers, LENGTH_OF_RANDOM_PASS);
			List<String> hashedPasswords = new ArrayList<>();
			List<String> saltList = new ArrayList<>();
			for(String item: passwords) {
				String salt = passwordService.getRandomString(SALT_BYTES);
				hashedPasswords.add(passwordService.pbkdf2(item, salt));
				saltList.add(salt);
			}
			userRepository.updateAllSaltColumn(saltList);
			userRepository.resetAllPassword(hashedPasswords);
			
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean addUser(User user, String salt) {
		try {
			userRepository.addUser(user);
			
			userRepository.updateSaltColumn(user.getUsername(), salt);
					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
    
}