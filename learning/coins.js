const getDenominations = (num) => {
  let res = [];
  if (num < 8) return false;
  if (num == 8) return [3, 5];
  if (num == 9) return [3, 3, 3];
  if (num == 10) return [5, 5];
  // if (num == 11) return [3, 3, 5];
  // if (num == 12) return [3, 3, 3, 3];
  res = getDenominations(num - 3);
  res.push(3);
  return res;
}

console.log(getDenominations(29));